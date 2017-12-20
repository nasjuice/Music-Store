package com.pandamedia.beans.purchasing;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * This class represents a credit card object, with getters and setters 
 * for the card numbers, cardholder name, type, security code, and expiry date.
 * 
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class CreditCard {
    private int cardMonth;
    private int cardYear;
    private String cardNum;
    private String cardType;
    private int cardCode;
    private String cardName;
    
    public int getCardMonth()
    {
        return cardMonth;
    }
    
    public void setCardMonth(int month)
    {
        this.cardMonth = month;
    }
    
    public int getCardYear()
    {
        return cardYear;
    }
    
    public void setCardYear(int year)
    {
        this.cardYear = year;
    }
    
    public String getCardNum()
    {
        return cardNum;
    }
    
    public void setCardNum(String cardNum)
    {
        this.cardNum = cardNum;
    }
    
    public String getCardType()
    {
        return cardType;
    }
    
    public void setCardType(String cardType)
    {
        this.cardType = cardType;
    }
    
    public int getCardCode()
    {
        return cardCode;
    }
    
    public void setCardCode(int cardCode)
    {
        this.cardCode = cardCode;
    }
    
    public String getCardName()
    {
        return cardName;
    }
    
    public void setCardName(String cardName)
    {
        this.cardName = cardName;
    }
}
