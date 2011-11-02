//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.11.02 at 03:58:03 PM MEZ 
//


package sernet.verinice.model.auth;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for configurationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="configurationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="blacklist"/>
 *     &lt;enumeration value="whitelist"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "configurationType")
@XmlEnum
public enum ConfigurationType {

    @XmlEnumValue("blacklist")
    BLACKLIST("blacklist"),
    @XmlEnumValue("whitelist")
    WHITELIST("whitelist");
    private final String value;

    ConfigurationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ConfigurationType fromValue(String v) {
        for (ConfigurationType c: ConfigurationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
