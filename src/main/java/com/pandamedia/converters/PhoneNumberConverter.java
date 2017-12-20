package com.pandamedia.converters;

import com.pandamedia.beans.PhoneNumberBean;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 * Converts Phone Number into properly formatted phone number.
 * @author Hau Gilles Che
 */
@FacesConverter(forClass = PhoneNumberBean.class)
public class PhoneNumberConverter implements Converter, Serializable{

    /**
     * Used when inputing a value Accept the string and check that includes
     * Digits or separators
     * @param context
     * @param component
     * @param value
     * @return 
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component
            , String newValue) {
        StringBuilder builder=new StringBuilder(newValue);
        boolean foundInvalidChar=false;
        char invalidChar='\0';
        int i=0;
        //signifies no input
        if(builder.length()<1)
            return null;
        while(i<builder.length() && !foundInvalidChar){
            char ch=builder.charAt(i);
            if(Character.isDigit(ch)){
                i++;
            }else if(Character.isWhitespace(ch)){
                builder.deleteCharAt(i);
            }else if(ch == '(' || ch == ')' || ch == '-' || ch == '+'){
                builder.deleteCharAt(i);
            }else{
                foundInvalidChar=true;
                invalidChar=ch;
            }
        }
        
        if(foundInvalidChar){
            FacesMessage msg=com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "badPhoneNumberCharacter"
                    ,new Object[] {invalidChar});
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }

        if(i < 10 || i > 11){
            FacesMessage msg=com.pandamedia.utilities.Messages.getMessage(
                    "bundles.messages", "badPhoneNumberLength", null);
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(msg);
        }

        return new PhoneNumberBean(builder.toString());
    }
    
    /**
     * Used when display the value inserts the separators.
     * @param context
     * @param component
     * @param value
     * @return 
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component
            , Object value) throws ConverterException{
        //length 10: (xxx) xxx-xxxx
        //length 11: +xxxx-xxx-xxxx
        if(!(value instanceof PhoneNumberBean))
            throw new ConverterException();
        String v = ((PhoneNumberBean)value).toString();
        int[] boundaries=null;
        char[] separators=null;
        int length=v.length();
        
        if(length == 10){
            boundaries=new int[]{0,3,3,6};
            separators=new char[]{'(',')',' ','-'};
        }else if(length == 11){
            boundaries=new int[]{0,4,7};
            separators=new char[]{'+','-','-'};
        }else
            return v;
        
        StringBuilder result=new StringBuilder();
        int start=0;
        for(int i=0;i<boundaries.length;i++){
            int end=boundaries[i];
            result.append(v.substring(start,end));
            result.append(separators[i]);
            start=end;
        }
        result.append(v.substring(start));
        return result.toString();
    }
    
}
