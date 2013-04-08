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

import com.jajja.arachne.exceptions.MalformedDomain;

public class Domain extends Host {

	private static Map<String, List<Suffix>> publicSuffices = Suffix.loadPublic();

	private static Map<String, List<Suffix>> privateSuffices = Suffix.loadPrivate();
	
	
	private String[] labels;
	
	private String fqdn;

	private String tld;
	
	private String sld;

	private Record publicRecord;
	
	private Record privateRecord;
	
	public Domain(String name) throws MalformedDomain {
		super(IDN.toASCII(name));
		parse();
		match();
	}
	
	public String getFqdn() {
		return fqdn;
	}
	
	public String getTld() {
		return tld;
	}
	
	public String getSld() {
		return sld;
	}

	public Record getPublicRecord() {
		return publicRecord;
	}

	public Record getPrivateRecord() {
		return privateRecord;
	}
	
	private void parse() throws MalformedDomain {
		fqdn = getName().toLowerCase();
		if (fqdn.isEmpty())
			throw new MalformedDomain(getName(), "Empty domain!");			
		labels = fqdn.split("\\."); // TODO: implement proper string split for performance
		if (253 < fqdn.length()) 
			throw new MalformedDomain(getName(), "Too many characters in fully qualified domain name!");
		if (127 < labels.length) 
			throw new MalformedDomain(getName(), "Too many labels in fully qualified domain name!");			
		for (String label : labels) {
			if (63 < label.length()) 
				throw new MalformedDomain(getName(), "Too many characters in domain name!");
			if (!label.matches("[0-9a-z-]+")) // TODO: compile this statically or search manually for performance
				throw new MalformedDomain(getName(), "Invalid charcters in domain name!");
		}
		tld = labels[labels.length - 1];			
		if (1 < labels.length) {
			sld = labels[labels.length - 2];
		}
	}
	
	private void match() {
		try {
			publicRecord = getRecord(publicSuffices.get(tld), labels);
			if (publicRecord != null) {
			    Record record = getRecord(privateSuffices.get(tld), labels);
			    if (record != null && !record.getEntry().equals("www." + publicRecord.getEntry())) {
			        privateRecord = record;
                }
			}
		} catch (Exception exception) {
			exception.printStackTrace();
//			log.warn("Failed to extract suffix or registry from " + string, exception);
		}
	}

	private Record getRecord(List<Suffix> rules, String[] labels) {
		Record record = null;
	    if (rules != null) {
	        for (Suffix rule : rules) {
	            record = rule.match(labels);
	            if (record != null) {
	                if (record.getEntry() == null) {
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
				+ ", tld => " + tld + ", sld => " + sld + ", publicRecord => "
				+ publicRecord + ", privateRecord => " + privateRecord + " }";
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(new Domain("peat.se"));
			System.out.println(new Domain("peat.wordpress.se"));
			System.out.println(new Domain("peat.wordpress.com"));
			System.out.println(new Domain("peat.co.uk"));
			System.out.println(new Domain("peat.co.se"));
			System.out.println(new Domain("pEaT.sE"));
		} catch (MalformedDomain e) {
			e.printStackTrace();
		}
	}
	
	private static class Suffix implements Comparable<Suffix> {
	    
		private String definition;
		
		private boolean isException;
		
		private String[] patterns;
		
		private Suffix(String definition) {
			this.definition = definition;
			isException = definition.startsWith("!");					
			patterns = IDN.toASCII(definition.replaceAll("[!]", "")).split("\\.");
		}
		
		public Record match(String[] labels) {
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
			if (isException) {
				if (labels.length < patterns.length) {
					return null;
				}
			} else {
				if (patterns.length < labels.length) {
					entry = labels[labels.length - (patterns.length + 1)] + '.' + entry;				
				} else {
					entry = "";
				}
			}
			Record match = new Record();
			match.setEntry(entry);
			if (!entry.isEmpty()) {
				match.setPattern(definition);
				match.setSuffix(isException ? entry : entry.substring(Math.max(0, entry.indexOf('.') + 1)));
			}
			return match;
		}
		
		public String getTld() {
			return patterns[patterns.length - 1];
		}
		
		public int weight() {
			return patterns.length + (isException ? 100000 : 0);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("[ definition => ");
			builder.append(definition);
			builder.append(", labels => ");
			builder.append(Arrays.asList(patterns));
			builder.append(", isException => ");
			builder.append(isException);
			builder.append(" ]");
			return builder.toString();
		}
		
		private static Log log = LogFactory.getLog(Suffix.class);
		
		public static Map<String, List<Suffix>> loadPublic() {
		    Map<String, List<Suffix>> map = new HashMap<String, List<Suffix>>();
		    add("/suffix/icann_effective_tld_names.dat", map);
		    add("/suffix/icann_patch_tld_names.dat", map);
		    for (String tld : map.keySet()) {
		        Collections.sort(map.get(tld));
		    }
		    return map;
		}
		
		public static Map<String, List<Suffix>> loadPrivate() {
		    Map<String, List<Suffix>> map = new HashMap<String, List<Suffix>>();
		    add("/suffix/private_effective_tld_names.dat", map);
		    add("/suffix/private_patch_tld_names.dat", map);
		    for (String tld : map.keySet()) {
		        Collections.sort(map.get(tld));
		    }
		    return map;
		}
		
		private static void add(String file,  Map<String, List<Suffix>> map) {
		    for (Suffix rule : read(file)) {
	            List<Suffix> rules = map.get(rule.getTld());
	            if (rules == null) {
	                rules = new LinkedList<Suffix>();
	                map.put(rule.getTld(), rules);
	            }
	            rules.add(rule);            
	        }
		}
		
		private static List<Suffix> read(String file) {
		    List<Suffix> rules = null;
		    try {
		        log.info("Trying file resource for " + file);
		        rules = read(new FileInputStream(new File("/var/lib/bruichladdich" + file)));
	        } catch (Exception e) {
	            log.info("Trying class path resource for effective TLD names " + file);
	            rules = read(Suffix.class.getResourceAsStream(file));
	        }
		    return rules != null ? rules : new LinkedList<Suffix>();
		}
		
		private static List<Suffix> read(InputStream inputStream) {
	        BufferedReader bufferedReader = null;
	        List<Suffix> rules = new LinkedList<Suffix>();
	        try {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
	            String line = null;
	            while ((line = bufferedReader.readLine()) != null) {
	                if (!line.matches("(\\s+.*)|(/+.*)") && !line.isEmpty()) {
	                    try {
	                        rules.add(new Suffix(line));                        
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
		public int compareTo(Suffix rule) {
			return rule.weight() - weight();
		}
		
	}
	
}
