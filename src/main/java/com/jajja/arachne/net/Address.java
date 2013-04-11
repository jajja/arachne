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
    private boolean isIpv4;
    private boolean isIpv6;
    
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
    
    public String getHex() {
        return hex;
    }
    
    /**
     * Tells whether the address name is IPv4 or not.
     * 
     * @return true if the address is IPv4, false otherwise
     */
    public boolean isIpv4() {
        return isIpv4;
    }
    
    /**
     * Tells whether the address name is IPv6 or not.
     * 
     * @return true if the address is IPv6, false otherwise
     */
    public boolean isIpv6() {
        return isIpv6;
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
        if (0 < getName().indexOf(".")) {
            hex = parseIpv4(address);
        } else if (0 < getName().indexOf(":")) {
            parseIpv6(address);
        } else {
            throw new MalformedAddress(address, "Not an address!");
        }
    }

    private static String parseIpv4(String address) throws MalformedAddress {
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
        return hex.toString();
    }
    
    public static void main(String[] args) {
        String[] ipv4s = new String[] {
                "127.0.0.1",
                "213.66.58.72",
                ".127.0.0.1",
                "127.0.0.1.",
                "127..0.1",
                "127.01.0.1",
                "127.0.0.0.1",
                "127.256.0.1",
                "0.127.0.1",
        };
        for (String ipv4 : ipv4s) {
            try {
                System.out.println(ipv4 + "\t: " + parseIpv4(ipv4));
            } catch (MalformedAddress e) {
                System.out.println(e.getAddress() + "\t: " + e.getMessage());
            }
        }
    }
    
    private static String parseIpv6(String address) throws MalformedAddress {
        throw new MalformedAddress(address, "Not implemented!"); // XXX: implement        
    }

    @Override
    public String toString() {
        return "{ hex => " + hex + ", isIpv4 => " + isIpv4 + ", isIpv6 => "
                + isIpv6 + " }";
    }
    
}
