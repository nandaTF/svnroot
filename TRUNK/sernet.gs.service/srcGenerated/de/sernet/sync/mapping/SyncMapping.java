
package de.sernet.sync.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mapObjectType" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="mapAttributeType" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="extId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="intId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="extId" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *                 &lt;attribute name="intId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "mapObjectType"
})
@XmlRootElement(name = "syncMapping")
public class SyncMapping implements Serializable {

    protected List<SyncMapping.MapObjectType> mapObjectType;

    /**
     * Gets the value of the mapObjectType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mapObjectType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapObjectType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SyncMapping.MapObjectType }
     * 
     * 
     */
    public List<SyncMapping.MapObjectType> getMapObjectType() {
        if (mapObjectType == null) {
            mapObjectType = new ArrayList<SyncMapping.MapObjectType>();
        }
        return this.mapObjectType;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="mapAttributeType" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="extId" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="intId" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="extId" use="required" type="{http://www.w3.org/2001/XMLSchema}ID" />
     *       &lt;attribute name="intId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "mapAttributeType"
    })
    public static class MapObjectType implements Serializable {

        protected List<SyncMapping.MapObjectType.MapAttributeType> mapAttributeType;
        @XmlAttribute(name = "extId", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlID
        @XmlSchemaType(name = "ID")
        protected String extId;
        @XmlAttribute(name = "intId", required = true)
        protected String intId;

        /**
         * Gets the value of the mapAttributeType property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the mapAttributeType property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getMapAttributeType().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SyncMapping.MapObjectType.MapAttributeType }
         * 
         * 
         */
        public List<SyncMapping.MapObjectType.MapAttributeType> getMapAttributeType() {
            if (mapAttributeType == null) {
                mapAttributeType = new ArrayList<SyncMapping.MapObjectType.MapAttributeType>();
            }
            return this.mapAttributeType;
        }

        /**
         * Gets the value of the extId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtId() {
            return extId;
        }

        /**
         * Sets the value of the extId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtId(String value) {
            this.extId = value;
        }

        /**
         * Gets the value of the intId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIntId() {
            return intId;
        }

        /**
         * Sets the value of the intId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIntId(String value) {
            this.intId = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="extId" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="intId" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class MapAttributeType implements Serializable {

            @XmlAttribute(name = "extId")
            protected String extId;
            @XmlAttribute(name = "intId")
            protected String intId;

            /**
             * Gets the value of the extId property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getExtId() {
                return extId;
            }

            /**
             * Sets the value of the extId property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setExtId(String value) {
                this.extId = value;
            }

            /**
             * Gets the value of the intId property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getIntId() {
                return intId;
            }

            /**
             * Sets the value of the intId property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setIntId(String value) {
                this.intId = value;
            }

        }

    }

}
