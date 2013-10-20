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

import com.jajja.arachne.exceptions.MalformedDomainException;
import com.jajja.arachne.exceptions.MalformedUriException;

// XXX: draft, might not make it to the release!
public class Endpoint {

    private String string;
    private Host host;
    private Integer port;

    public Endpoint(String string) throws MalformedUriException {
        this.string = string;
        try {
            parse();
        } catch (MalformedDomainException e) {
            throw new MalformedUriException(string, "Failed to parse host!", e);
        }
    }

    public String getString() {
        return string;
    }

    public Host getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    private void parse() throws MalformedUriException, MalformedDomainException {
        int colon = string.lastIndexOf(':');
        if (string.charAt(0) == '[') {
            int bracket = string.indexOf(']');
            if (bracket < 0)
                throw new MalformedUriException(string, "Unmatched escape bracket in IPv6 endpoint!");
            if (bracket != colon - 1)
                throw new MalformedUriException(string, "Expected colon after IPv6 host-part!");
            if (-1 < string.indexOf('.'))
                throw new MalformedUriException(string, "Detected escape brackets for non IPv6 host!");
            host = Host.get(string.substring(1, bracket));
        } else if (-1 < colon) {
            host = Host.get(string.substring(0, colon));
        } else {
            host = Host.get(string);
        }
        if (-1 < colon) {
            try {
                port = Integer.parseInt(string.substring(colon + 1));
            } catch (NumberFormatException e) {
                throw new MalformedUriException(string, "Invalid port number format!");
            }
            if (port < 1 || 65535 < port)
                throw new MalformedUriException(string, "Port number out of range!");
        }
    }

    @Override
    public String toString() {
        return string;
    }

    public static void main(String[] args) {
        for (String string : new String[] {
                "[::1]:80",
                "[::1] :80",
                "[127.0.0.1]:80",
                "127.0.0.1:80",
                "127.0.0.1",
                "[::1:80",
                "[::1]:0",
                "[::1]:lol",
        }) try {
            System.out.println(new Endpoint(string));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

}
