/*
 * Copyright (C) 2013 Jajja Communications AB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.jajja.arachne.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.IDN;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jajja.arachne.exceptions.MalformedDomainException;

/**
 * A class for parsing Internet domains and matching public suffix records. The
 * public suffix records can be used to group domains by site or to avoid
 * privacy-damaging cookies related to domain hierarchy.
 *
 * The public suffix implementation of this class is based on the public suffix
 * reference initiative by Mozilla. The original public suffix list has been
 * split into ICANN "registered" and private "subleased" records. Several
 * private records such as *.wordpress.com or the like have been added to the
 * private list, and the ICANN list has been augmented with missing entries.
 *
 * For reference check out: http://publicsuffix.org
 *
 * @author Martin Korinth <martin.korinth@jajja.com>
 */
public class Domain extends Host {

    private static Log log = LogFactory.getLog(Domain.class);
    private static Map<String, List<Rule>> icannRules = Rule.loadIcann();
    private static Map<String, List<Rule>> privateRules = Rule.loadPrivate();

    private String[] labels;
    private boolean isMatched = false;

    private String fqdn;
    private String tld;
    private Record _registeredRecord;
    private Record _subleasedRecord;
    private String _publicSuffix;

    /**
     * Creates a domain by parsing the name and matching records from the public
     * suffix lists for ICANN "registered" and private "subleased" records.
     *
     * @param name
     *            the domain name
     * @throws MalformedDomainException
     *             when the domain name can not be parsed as a domain
     */
    public Domain(String name) throws MalformedDomainException {
        super(IDN.toASCII(name));
        parse();
    }

    /**
     * Provides the fully qualified domain name of the domain, all in lower-case
     * letters.
     *
     * @return the fully qualified domain name
     */
    public String getFqdn() {
        return fqdn;
    }

    /**
     * Provides the top level domain (name) of the domain, all in lower-case letters.
     *
     * @return the top level domain
     */
    public String getTld() {
        return tld;
    }

    /**
     * Provides the suffix of the most specific existing record of the domain.
     *
     * @return the suffix of the most specific existing record of the domain if
     *         such exists, otherwise null
     */
    public String getSuffix() {
        Record record = getRecord();
        return record != null ? record.getSuffix() : null;
    }

    /**
     * Provides the entry of the most specific existing record of the domain.
     *
     * @return the suffix of the most specific existing record of the domain if
     *         such exists, otherwise null
     */
    public String getEntry() {
        Record record = getRecord();
        return record != null ? record.getEntry() : null;
    }

    /**
     * Provides the matched rule of the most specific existing record of the
     * domain.
     *
     * @return the matched rule of the most specific existing record of the
     *         domain if such exists, otherwise null
     */
    public String getRule() {
        Record record = getRecord();
        return record != null ? record.getRule() : null;
    }


    /**
     * Provides the matched public suffix in the ICANN part of the list,
     * regardless of whether a record was matched or not.
     *
     * @return the matched public suffix, or null for no matched public suffix
     */
    public String getPublicSuffix() {
        match();
        return _publicSuffix;
    }

    /**
     * Provides the most specific existing record of the domain, which can be
     * used to group domains by site or avoid privacy-damaging cookies.
     *
     * @return the most specific record match, the subleased record if such
     *         exist, the registered record if such exists, otherwise null
     */
    public Record getRecord() {
        return isSubleased() ? getSubleasedRecord() : getRegisteredRecord();
    }

    /**
     * Provides the registered record of the domain if such exists.
     *
     * @return the registered record if such exists, otherwise null
     */
    public Record getRegisteredRecord() {
        match();
        return _registeredRecord;
    }

    /**
     * Provides the subleased record of the domain if such exists.
     *
     * @return the subleased record if such exists, otherwise null
     */
    public Record getSubleasedRecord() {
        match();
        return _subleasedRecord;
    }

    /**
     * Tells whether the domain is a registered or not. A domain is registered
     * if it is a subdomain to a reserved public prefix according to ICANN.
     *
     * @return true if the domain is registered, false otherwise
     */
    public boolean isRegistered() {
        return getRegisteredRecord() != null;
    }

    /**
     * Tells whether the domain is subleased or not. A domain is subleased if it
     * is a subdomain to a registered domain known to provide subdomains to
     * externally or internally hosted autonomous sites.
     *
     * @return true if the domain is subleased, false otherwise
     */
    public boolean isSubleased() {
        return getSubleasedRecord() != null;
    }

    private void parse() throws MalformedDomainException {
        fqdn = getString().toLowerCase();
        if (fqdn.isEmpty())
            throw new MalformedDomainException(getString(), "Empty domain!");
        labels = fqdn.split("\\."); // TODO: implement proper string split for performance
        if (253 < fqdn.length())
            throw new MalformedDomainException(getString(), "Too many characters in fully qualified domain name!");
        if (127 < labels.length)
            throw new MalformedDomainException(getString(), "Too many labels in fully qualified domain name!");
        for (String label : labels) {
            if (63 < label.length())
                throw new MalformedDomainException(getString(), "Too many characters in domain name!");
            if (!label.matches("[0-9a-z-]+")) // TODO: compile this statically or search manually for performance
                throw new MalformedDomainException(getString(), "Invalid charcters in domain name!");
        }
        tld = labels[labels.length - 1];
    }

    private void match() {
        if (!isMatched) {
            _registeredRecord = getRecord(labels, true);
            if (_registeredRecord != null) {
                Record record = getRecord(labels, false);
                if (record != null && !record.getEntry().equals("www." + _registeredRecord.getEntry())) {
                    _subleasedRecord = record;
                }
            }
            isMatched = true;
        }
    }

    private Record getRecord(String[] labels, boolean isPublic) {
        Record record = null;
        List<Rule> rules = isPublic ? icannRules.get(tld) : privateRules.get(tld);
        if (rules != null) {
            for (Rule rule : rules) {
                record = rule.match(labels);
                if (record != null) {
                    if (isPublic) {
                        this._publicSuffix = record.getRule();
                    }
                    if (record.getEntry().isEmpty()) {
                        record = null;
                    }
                    break;
                }
            }
        }
        return record;
    }

    @Override
    public String toString() {
        return "{ fqdn => " + fqdn + ", labels => " + Arrays.toString(labels)
                + ", tld => " + tld + ", record => "
                + getRecord() + ", isSubleased => " + isSubleased() + ", publicSuffix => " + getPublicSuffix() + " }";
    }

    public static void main(String[] args) {
        try {
//            System.out.println(new Domain("peat.se"));
//            System.out.println(new Domain("peat.wordpress.se"));
//            System.out.println(new Domain("peat.wordpress.com"));
//            System.out.println(new Domain("peat.co.uk"));
//            System.out.println(new Domain("peat.co.se"));
//            System.out.println(new Domain("pEaT.sE"));
//            System.out.println(new Domain("peat.local"));
//            System.out.println(new Domain("local"));
            System.out.println(new Domain("lol.lol.mil.no"));
            System.out.println(new Domain("lol.mil.no"));
            System.out.println(new Domain("mil.no"));
        } catch (MalformedDomainException e) {
            e.printStackTrace();
        }
    }

    private static class Rule implements Comparable<Rule> {

        private String rule;

        private boolean isException;

        private boolean isExact;

        private String[] patterns;

        private Rule(String rule) {
            this.rule = rule;
            isException = rule.startsWith("!");
            isExact = rule.startsWith("?");
            isException = isException || isExact;
            patterns = IDN.toASCII(rule.replaceAll("[!?]", "")).split("\\.");
        }

        Record match(String[] labels) {
            String entry = "";
            for (int i = 0; i < Math.min(patterns.length, labels.length); i++) {
                String pattern = patterns[patterns.length - (i + 1)];
                String label = labels[labels.length - (i + 1)];
                if ("*".equals(pattern) || label.equals(pattern)) {
                    entry = label + (entry.isEmpty() ? "" : '.') + entry;
                } else {
                    return null;
                }
            }
            String suffix = null;
            if (isException) {
                if (labels.length < patterns.length) {
                    return null;
                } else {
                    if (isExact && labels.length != patterns.length) {
                        return null;
                    }
                    suffix = isExact ? entry.substring(Math.max(0, entry.indexOf('.') + 1)) : entry;
                }
            } else {
                if (patterns.length < labels.length) {
                    suffix = entry;
                    entry = labels[labels.length - (patterns.length + 1)] + '.' + entry;
                } else {
                    entry = "";
                }
            }
            Record record = new Record();
            record.setEntry(entry);
            record.setSuffix(suffix);
            record.setRule(rule);
            return record;
        }

        private String getTld() {
            return patterns[patterns.length - 1];
        }

        private int weight() {
            return patterns.length + (isException ? 255 : 0) + (isExact ? 255 : 0);
        }

        static Map<String, List<Rule>> loadIcann() {
            Map<String, List<Rule>> map = new HashMap<String, List<Rule>>();
            add("/suffix/icann_effective_tld_names.dat", map);
            add("/suffix/icann_patch_tld_names.dat", map);
            for (String tld : map.keySet()) {
                Collections.sort(map.get(tld));
            }
            return map;
        }

        static Map<String, List<Rule>> loadPrivate() {
            Map<String, List<Rule>> map = new HashMap<String, List<Rule>>();
            add("/suffix/private_effective_tld_names.dat", map);
            add("/suffix/private_patch_tld_names.dat", map);
            for (String tld : map.keySet()) {
                Collections.sort(map.get(tld));
            }
            return map;
        }

        private static void add(String file,  Map<String, List<Rule>> map) {
            for (Rule rule : read(file)) {
                List<Rule> rules = map.get(rule.getTld());
                if (rules == null) {
                    rules = new LinkedList<Rule>();
                    map.put(rule.getTld(), rules);
                }
                rules.add(rule);
            }
        }

        private static List<Rule> read(String file) {
            List<Rule> rules = null;
            try {
                log.info("Trying file resource for " + file);
                rules = read(new FileInputStream(new File("/usr/share/arachne" + file)));
            } catch (Exception e) {
                log.info("Trying class path resource for effective TLD names " + file);
                rules = read(Rule.class.getResourceAsStream(file));
            }
            return rules != null ? rules : new LinkedList<Rule>();
        }

        private static List<Rule> read(InputStream inputStream) {
            BufferedReader bufferedReader = null;
            List<Rule> rules = new LinkedList<Rule>();
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.matches("(\\s+.*)|(/+.*)") && !line.isEmpty()) {
                        try {
                            rules.add(new Rule(line.trim()));
                        } catch (Exception e) {
                            log.error("Failed to parse public domain suffix rule from line: " + line, e);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to read line", e);
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        log.warn("Failed to close reader", e);
                    }
                }
            }
            return rules;
        }

        @Override
        public int compareTo(Rule rule) {
            return rule.weight() - weight();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ definition => ");
            builder.append(rule);
            builder.append(", labels => ");
            builder.append(Arrays.asList(patterns));
            builder.append(", isException => ");
            builder.append(isException);
            builder.append(" ]");
            return builder.toString();
        }

    }

}
