import org.junit.Assert;
import org.junit.Test;

import com.jajja.arachne.exceptions.MalformedAddressException;
import com.jajja.arachne.net.Address;

public class AddressTest {
    
    @Test public void ipv4() { // Correct IPv4 addresses
        try {
            Assert.assertEquals(Address.parseIpv4("127.0.0.1"), new Address("127.0.0.1").getHex());
            Assert.assertEquals(Address.parseIpv4("213.66.58.72"), new Address("213.66.58.72").getHex());
            Assert.assertEquals(Address.parseIpv4("0.0.0.1"), new Address("127.0.0.1").getHex());
        } catch (MalformedAddressException e) {
            Assert.assertTrue(false);
        }   
    }

    @Test public void ipv6() { // Correct IPv6 addresses
        try {
            Assert.assertEquals(Address.parseIpv6("::1"), new Address("::1").getHex());
            Assert.assertEquals(Address.parseIpv6("fe80::1%lo0"), new Address("fe80::1%lo0").getHex());
            Assert.assertEquals(Address.parseIpv6("2605:2700:0:3::4713:93e3"), new Address("2605:2700:0:3::4713:93e3").getHex());
        } catch (MalformedAddressException e) {
            Assert.assertTrue(false);
        }   
    }

    
    @Test public void leadingDotIpv4() { // Leading dot IPv4
        try {
            Address.parseIpv4(".127.0.0.1");
            Assert.assertTrue(false);
        } catch (MalformedAddressException e) {
            System.out.println("Leading dot ipv4 (" + e.getAddress() + "): " + e.getMessage());
        }   
    }
    
    @Test public void emptySubNetIpv4() { // Empty sub-net IPv4
        try {
            Address.parseIpv4("127..0.1");
            Assert.assertTrue(false);
        } catch (MalformedAddressException e) {
            System.out.println("Empty sub-net ipv4 (" + e.getAddress() + "): " + e.getMessage());
        }   
    }
    
    @Test public void paddedSubNetIpv4() { // Padded sub-net IPv4
        try {
            Address.parseIpv4("127.01.0.1");
            Assert.assertTrue(false);
        } catch (MalformedAddressException e) {
            System.out.println("Padded sub-net ipv4 (" + e.getAddress() + "): " + e.getMessage());
        }   
    }
    
    @Test public void largeSubNetIpv4() { // Large sub-net IPv4
        try {
            Address.parseIpv4("127.0.0.256");
            Assert.assertTrue(false);
        } catch (MalformedAddressException e) {
            System.out.println("Large sub-net ipv4 (" + e.getAddress() + "): " + e.getMessage());
        }   
    }
    
}
