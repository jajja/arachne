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

import com.jajja.arachne.exceptions.MalformedAddress;

/**
 * A class for parsing Internet addresses.
 * 
 * @author Martin Korinth <martin.korinth@jajja.com>
 */
public class Address extends Host {
    
    private String hex;
    private String comment;
    private int ipv;   
    
    /**
     * Creates an address by parsing the name.
     * 
     * @param name
     *            the domain name
     * @throws MalformedAddress
     *             when the address name can not be parsed as an address
     */
    public Address(String name) throws MalformedAddress {
        super(name);
        parse();
    }
    
    /**
     * Provides the hexadecimal representation parsed from the address.
     * 
     * @return the hexadecimal representation
     */
    public String getHex() {
        return hex;
    }
    
    
    /**
     * Provides any comment parsed from the address.
     * 
     * @return the comment of the address or null for no comment
     */
    public String getComment() {
        return comment;
    }
    
    /**
     * Tells whether the address name is IPv4 or not.
     * 
     * @return true if the address is IPv4, false otherwise
     */
    public boolean isIpv4() {
        return ipv == 4;
    }
    
    /**
     * Tells whether the address name is IPv6 or not.
     * 
     * @return true if the address is IPv6, false otherwise
     */
    public boolean isIpv6() {
        return ipv == 6;
    }
    
    /**
     * Tells whether a host name is an IPv4 address or not.
     * 
     * @param name
     *            the host name
     * @return true if the host name corresponds to an IPv4 address, false
     *         otherwise
     */
    public static boolean isIpv4(String name) {
        try {
            parseIpv4(name);
            return true;
        } catch (MalformedAddress malformedAdress) {
            return false;
        }
    }
    
    /**
     * Tells whether a host name is an IPv6 address or not.
     * 
     * @param name
     *            the host name
     * @return true if the host name corresponds to an IPv6 address, false
     *         otherwise
     */
    public static boolean isIpv6(String name) {
        try {
            parseIpv6(name);
            return true;
        } catch (MalformedAddress malformedAdress) {
            return false;
        }
    }
    
    /**
     * Parses the hexadecimal representation of an IPv4 address.
     * 
     * @param name
     *            the host name representing an IPv4 address
     * @return the hexadecimal representation
     * @throws MalformedAddress
     *             when an IPv4 address cannot be parsed by the given host name
     */
    public static String parseIpv4(String name) throws MalformedAddress {
        return new Address(name).parseIpv4().hex;
    }
    
    /**
     * Parses the hexadecimal representation of an IPv6 address.
     * 
     * @param name
     *            the host name representing an IPv6 address
     * @return the hexadecimal representation
     * @throws MalformedAddress
     *             when an IPv6 address cannot be parsed by the given host name
     */
    public static String parseIpv6(String name) throws MalformedAddress {
         return new Address(name).parseIpv6().hex;
    }

    /**
     * Tells whether a host name is an address or not.
     * 
     * @param name
     *            the host name
     * @return true if the host name corresponds to either an IPv4 or an IPv6
     *         address, false otherwise
     */
    public static boolean isAddress(String name) {
        return isIpv4(name) || isIpv6(name);
    }
    
    private void parse() throws MalformedAddress {
        String address = getName();
        if (-1 < getName().indexOf(".")) {
            parseIpv4();
        } else if (-1 < getName().indexOf(":")) {
            parseIpv6();
        } else {
            throw new MalformedAddress(address, "Not an address!");
        }
    }

    private Address parseIpv4() throws MalformedAddress {
        String address = getName();
        StringBuilder hex = new StringBuilder(); 
        int mask = 0;
        int subnet = 'a';
        int digits = 0;
        for (char c : address.toCharArray()) {          
            switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                if (0 < digits && mask == 0)
                    throw new MalformedAddress(address, "Zero-padded IPv4 address subnet!");  
                mask *= 10;
                mask += (int) c - 48;
                digits++;
                if (255 < mask)
                    throw new MalformedAddress(address, "An IPv4 subnet (i.e. the " + (char) subnet + "-net) can not exceed 255!");  
                break;
            case '.':
                if ('c' < subnet)
                    throw new MalformedAddress(address, "Too many subnets for an IPv4 address!");  
                if (digits == 0)
                    throw new MalformedAddress(address, "Empty IPv4 address " + (char) subnet + "-net!");  
                if (subnet == 'a' && mask == 0) 
                    throw new MalformedAddress(address, "Zero-leading IPv4 address a-net!");  
                hex.append(String.format("%02x", mask));
                subnet++;
                digits = 0;
                mask = 0;
                break;
            default:
                throw new MalformedAddress(address, "Illegal characters for an IPv4 address!");
            }
        }
        hex.append(String.format("%02x", mask));
        this.hex = hex.toString();
        ipv = 4;
        return this;
    }
    
    private Address parseIpv6() throws MalformedAddress {
        String address = getName();
        StringBuilder suffhex = new StringBuilder(); 
        StringBuilder prefhex = new StringBuilder(); 
        StringBuilder comment = new StringBuilder(); 
        int mask = 0;
        char d = 0;
        boolean isPadded = false;
        boolean isComment = false;
        for (char c : getName().toCharArray()) {   
            if (isComment) {
                comment.append(c);
            } else {
                switch (c) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    mask *= 16;
                    mask += Integer.parseInt("" + c, 16);
                    break;
                case ':':
                    if (d == ':') {
                        if (isPadded) 
                            throw new MalformedAddress(address, "IPv6 adress can only be zero-padded at one point!");
                        isPadded = true;
                    } else {
                        if (isPadded) {
                            suffhex.append(String.format("%04x", mask));                    
                        } else {
                            prefhex.append(String.format("%04x", mask));                                        
                        }                    
                    }
                    mask = 0;
                    break;
                case '%':
                    isComment = true;
                    break;
                default:
                    throw new MalformedAddress(address, "Illegal characters for an IPv4 address!");
                }
                d = c; // step!
            }
        }            
        if (isPadded) {
            suffhex.append(String.format("%04x", mask));                    
        } else {
            prefhex.append(String.format("%04x", mask));                                        
        }
        int padding = 32 - (prefhex.length() + suffhex.length());
        if (padding < 0)
            throw  new MalformedAddress(address, "Too large data for an IPv6 adress!");
        hex = String.format("%s%0" + padding  + "x%s", prefhex, 0, suffhex);
        if (0 < comment.length()) {
            this.comment = comment.toString();            
        }
        ipv = 6;
        return this;
    }

    @Override
    public String toString() {
        return "{ hex => " + hex + ", ipv => " + ipv + ", comment => " + comment + " }";
    }
    
    public static void main(String[] args) { // TODO: create unit tests instead
        String[] names = new String[] {
                "127.0.0.1",
                "213.66.58.72",
                ".127.0.0.1",
                "127.0.0.1.",
                "127..0.1",
                "127.01.0.1",
                "127.0.0.0.1",
                "127.256.0.1",
                "0.127.0.1",
                "::1",
                "fe80::1%lo0",
                "2605:2700:0:3::4713:93e3",
        };
        for (String name : names) {
            try {
                System.out.println(new Address(name));
            } catch (MalformedAddress e) {
                System.out.println(e.getAddress() + "\t: " + e.getMessage());
            }
        }
    }
    
}
