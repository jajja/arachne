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
import com.jajja.arachne.exceptions.MalformedDomain;

import static com.jajja.arachne.net.Address.*;

/**
 * An abstract Internet host, which can be either an Address or a Domain, as
 * entities regulated by ICANN.
 * 
 * @author Martin Korinth <martin.korinth@jajja.com>
 */
public abstract class Host {
    
    /**
     * Facilitates creation of a host according to the given host name.
     * 
     * @param name
     *            the host name
     * @return the host represented by either an address or a domain
     * @throws MalformedDomain
     *             when the name can not be resolved as an address and is not a
     *             legal domain.
     */
    public static Host get(String name) throws MalformedDomain {
        Host host = null;
        if (isAddress(name)) {
            try {
                return new Address(name);
            } catch (MalformedAddress e) {
                throw new IllegalStateException("Call pest control, there is a bug!", e); // cannot happen?!
            }
        }
        return host != null ? host : new Domain(name);
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

    private String name;
    
    Host(String name) {
        this.name = name;
    }
    
    /**
     * Provides the host name.
     * 
     * @return the name of the host
     */
    public String getName() {
        return name;
    }

}
