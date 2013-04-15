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

import com.jajja.arachne.exceptions.MalformedException;

// XXX: draft, might not make it to the release!
public class Endpoint {
    
    private String string;
    private Host host;
    private Integer port;
    private int defaultPort;
    
    public Endpoint(String string, int defaultPort) throws MalformedException {
        this.string = string;
        this.defaultPort = defaultPort;
        parse();
    }
    
    public String getString() {
        return string;
    }

    public Host getHost() {
        return host;
    }
    
    public int getPort() {
        return isDefaultPort() ? defaultPort : port;
    }
    
    public boolean isDefaultPort() {
        return port == null;
    }
    
    private void parse() throws MalformedException {
        int colon = string.lastIndexOf(':'); 
        if (string.charAt(0) == '[') {
            int bracket = string.indexOf(']');
            if (bracket < 0)
                throw new MalformedException("Unmatched escape bracket in IPv6 endpoint! " + string);
            if (bracket != colon - 1)
                throw new MalformedException("Expected colon after IPv6 host-part! " + string);
            if (-1 < string.indexOf('.'))
                throw new MalformedException("Detected escape brackets for non IPv6 host! " + string);  
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
                throw new MalformedException("Invalid port number format! " + string);
            }
            if (port < 1 || 65535 < port)
                throw new MalformedException("Port number out of range! " + string);            
        }
    }

    @Override
    public String toString() {
        return "{ string => " + string + ", host => " + host + ", port => " + getPort() + " }";
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
            System.out.println(new Endpoint(string, 80));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
    
}
