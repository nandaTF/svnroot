
package de.sernet.sync.sync;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="replyMessage" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *         &lt;element name="inserted" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="updated" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="deleted" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "replyMessage",
    "inserted",
    "updated",
    "deleted"
})
@XmlRootElement(name = "syncResponse")
public class SyncResponse {

    @XmlElement(required = true)
    protected List<String> replyMessage;
    protected int inserted;
    protected int updated;
    protected int deleted;

    /**
     * Gets the value of the replyMessage property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the replyMessage property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReplyMessage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getReplyMessage() {
        if (replyMessage == null) {
            replyMessage = new ArrayList<String>();
        }
        return this.replyMessage;
    }

    /**
     * Gets the value of the inserted property.
     * 
     */
    public int getInserted() {
        return inserted;
    }

    /**
     * Sets the value of the inserted property.
     * 
     */
    public void setInserted(int value) {
        this.inserted = value;
    }

    /**
     * Gets the value of the updated property.
     * 
     */
    public int getUpdated() {
        return updated;
    }

    /**
     * Sets the value of the updated property.
     * 
     */
    public void setUpdated(int value) {
        this.updated = value;
    }

    /**
     * Gets the value of the deleted property.
     * 
     */
    public int getDeleted() {
        return deleted;
    }

    /**
     * Sets the value of the deleted property.
     * 
     */
    public void setDeleted(int value) {
        this.deleted = value;
    }

}
