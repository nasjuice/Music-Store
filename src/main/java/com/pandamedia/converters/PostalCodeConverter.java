package com.pandamedia.converters;

import com.pandamedia.beans.PostalCodeBean;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 * Converts Postal Code into properly formatted postal code.
 * @author Hau Gilles Che
 */
@FacesConverter(forClass = PostalCodeBean.class)
public class PostalCodeConverter implements Converter, Serializable {

    private String separator;

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    /**
     * Used when inputing a value Accept the string and check that includes
     * character or whitespace
     *
     * @param context
     * @param component
     * @param newValue
     * @return
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
            String newValue) throws ConverterException {
        StringBuilder builder = new StringBuilder(newValue);
        boolean foundInvalidChar = false;
        char invalidChar = '\0';
        int i = 0;
        //signifies no input
        if(builder.length()<1)
            return null;
        while (i < builder.length() && !foundInvalidChar) {
            char ch = builder.charAt(i);
            if (i % 2 == 0) {
                if (Character.isLetter(ch)) {
                    if (Character.isLowerCase(ch)) {
                        char tempChar = Character.toUpperCase(ch);
                        builder.setCharAt(i, tempChar);
                    }
                    i++;
                } else {
                    foundInvalidChar = true;
                    invalidChar = ch;
                }
            } else {
                if (Character.isDigit(ch)) {
                    i++;
                } else if (Character.isWhitespace(ch)) {
                    builder.deleteCharAt(i);
                } else {
                    foundInvalidChar = true;
                    invalidChar = ch;
                }
            }
        }
        if (foundInvalidChar) {
            FacesMessage msg = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "badPostalCodeCharacter",
                    new Object[]{invalidChar});
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }
        
        //checks for invalid amount of characters
        if (i != 6) {
            FacesMessage msg = com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "badPostalCodeLength", new Object[]{i});
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }

        return new PostalCodeBean(builder.toString());
    }

    /**
     * Used when display the value Depending on the length of the string add
     * space
     *
     * @param context
     * @param component
     * @param value
     * @return
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component,
            Object value) throws ConverterException {
        if (!(value instanceof PostalCodeBean)) {
            throw new ConverterException();
        }
        String v = ((PostalCodeBean) value).toString();
        String sep = separator;
        if (sep == null) {
            sep = " ";
        }
        //if length is 7, space is already there
        if (v.length() == 7) {
            return v;
        }

        //inserts the space in the middle
        StringBuilder result = new StringBuilder();
        result.append(v.substring(0, 3));
        result.append(sep);
        result.append(v.substring(3));

        return result.toString();
    }
}
