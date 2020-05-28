/*
 * Create by platform eight on 2019. 10. 22.
 * Copyright (c) 2019. platform eight. All rights reserved.
 */

package com.platformeight.medimap;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;


public class NetworkTask extends AsyncTask<Void, Void, String> {

    private LatLng latLng;
    private String url;
    private JSONObject val;
    double atitude;
    double latitude;

    public NetworkTask(){

    }
    public NetworkTask(LatLng latLng){
        this.latLng = latLng;
    }
    public NetworkTask(double atitude, double latitude) {
        this.atitude = atitude;
        this.latitude = latitude;
    }

    @Override
    protected String doInBackground(Void... String) {
        String result; // 요청 결과를 저장할 변수.
        result = ApiRequest.run(latLng);
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
        //tv_outPut.setText(s);
    }
}