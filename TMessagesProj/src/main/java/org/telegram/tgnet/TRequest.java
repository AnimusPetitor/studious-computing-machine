package org.telegram.tgnet;

import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.ApplicationLoader;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TRequest  {

    private static RequestQueue queue = Volley.newRequestQueue(ApplicationLoader.applicationContext);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static <T extends TelegraphObject> Map<String,String> param(String method) throws JSONException {
        Map<String,String> m = new HashMap<>();
        JSONObject o = new JSONObject(method);
        Iterator keys = o.keys();
        while(keys.hasNext()){
            String k = (String) keys.next();
            try {
                m.put(k,o.getString(k));
            } catch (JSONException e) {
               Log.d("Mar",e.toString());
            }
        }
        return m;
    }

    public static <T extends TelegraphObject> void execute(final TelegraphMethod<T> method, Response.Listener<String> r,final OnPublished p) {
            String url = "https://api.telegra.ph/" + method.getUrlPath();
        //try {
            //Log.d("Philingramart", objectMapper.writeValueAsString(method));
        //} catch (JsonProcessingException e) {
          //  e.printStackTrace();
        //}

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, r,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            p.onPublished(null);
                           Log.e("MAR",error.toString());
                        }
                    }){
                @Override
                protected Map<String, String> getParams() {
                    try {
                        return param(objectMapper.writeValueAsString(method));
                    } catch (Exception e) {
                        Log.d("PARAMS",e.toString());
                    }
                    return  null;
                }
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("charset", "UTF-8");
                    params.put("Content-Type", "application/json; charset=ISO-8859-1");
                    return params;
                }
            };
            queue.add(postRequest);
        }

        public interface OnPublished{
            void onPublished(Page e);
        }


        public static void  shave(final List<Node> body,final String title,final String name,final String url,final OnPublished pub){
        Log.d("MARR","skfljsdf");
        final CreateAccount method =new CreateAccount(name).setAuthorName(name);
        //if(existing account or editing) edit
        TRequest.execute(method, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("MAR",method.toString()+":"+response);
                    final Account account= method.deserializeResponse(response);

                    final CreatePage p = new CreatePage(account.getAccessToken(), title, body)
                            .setAuthorName(name)
                            .setReturnContent(true);

                    if(url!=null || !url.startsWith("http")) {p.setAuthorUrl(url);}
                    TRequest.execute(p, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{
                                Page page = p.deserializeResponse(response);
                                //page.getUrl()
                                //(body.length() > 100 ? body.subSequence(0, 100) : body)
                                 //deleg.onPublished();
                                for(int i = 0; i< page.getContent().size(); i++ ){
                                    Log.d("MAR",page.getContent().get(i)+"");
                                }
                                Log.d("MAR",page.getPath()+"");

                                //(body.length() > 100 ? body.subSequence(0, 100) : body)
                                String l = page.getUrl();
                                FileOutputStream fi = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Telegram/Telegraph/" + page.getTitle() +".-_-." + Base64.encodeToString(l.getBytes(),0,l.length(),0) + ".pub");
                                ObjectOutputStream oo = new ObjectOutputStream(fi);
                                oo.writeObject(page);
                                pub.onPublished(page);

                                FileOutputStream rec = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Telegram/.last");
                                ObjectOutputStream oo1 = new ObjectOutputStream(rec);
                                oo1.writeObject(new String[]{name,url,account.getAccessToken()});
                                //AndroidUtilities.copyFile(new ByteArrayInputStream(body.toString().getBytes()), new File();
                            } catch (Exception e) {
                                pub.onPublished(null);
                                Log.d("MAR",e.toString());
                            }
                        }
                    },pub);
                } catch (TelegraphRequestException e) {
                    pub.onPublished(null);
                    Log.d("MAR",e.toString());
                }
            }
        },pub);
    }
}