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
        return false; // TODO: implement
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
        return false; // TODO: implement
    }
    
    private void parse() throws MalformedAddress {
        // TODO: implement
    }
}
