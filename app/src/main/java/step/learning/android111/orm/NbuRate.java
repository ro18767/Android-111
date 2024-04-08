package step.learning.android111.orm;

import org.json.JSONException;
import org.json.JSONObject;

public class NbuRate {
    private int r030;
    private String txt;
    private double rate;
    private String cc;
    private String exchangeDate;

    public static NbuRate fromJson(JSONObject jsonObject) throws JSONException {
        NbuRate rate = new NbuRate();
        rate.setR030( jsonObject.getInt("r030") ) ;
        rate.setTxt( jsonObject.getString("txt") );
        rate.setRate( jsonObject.getDouble("rate") );
        rate.setCc( jsonObject.getString("cc") );
        rate.setExchangeDate( jsonObject.getString("exchangedate") );
        return rate;
    }

    public int getR030() {
        return r030;
    }

    public void setR030(int r030) {
        this.r030 = r030;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getExchangeDate() {
        return exchangeDate;
    }

    public void setExchangeDate(String exchangeDate) {
        this.exchangeDate = exchangeDate;
    }
}
/*
ORM для курсів валют Нацбанку, що находять у JSON
  {
    "r030": 944,
    "txt": "Азербайджанський манат",
    "rate": 22.9491,
    "cc": "AZN",
    "exchangedate": "09.04.2024"
  },
 */