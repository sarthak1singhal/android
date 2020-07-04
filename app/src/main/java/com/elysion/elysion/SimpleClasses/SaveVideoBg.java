package com.elysion.elysion.SimpleClasses;

import android.content.Context;
import android.os.AsyncTask;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SaveVideoBg extends AsyncTask<String, Void, String> {

    private Exception exception;
    Context context;
    public  SaveVideoBg(Context c){
        context = c;
    }

    protected String doInBackground(String... urls) {
        try {
            URL url = new URL(cachingUrl(urls[0]));



            InputStream inputStream = url.openStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                //nothing to do
            }

            return "";


        } catch (Exception e) {
            this.exception = e;

            return null;
        } finally {
         }
    }
    public String cachingUrl(String urlPath) {
        return TicTic.getProxy(context).getProxyUrl(urlPath, true);
    }

    protected void onPostExecute(String feed) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }
}