package com.fxb.receiver;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.uhf.magic.reader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dxl on 2017-06-26.
 */

public class ReceiverWriteActivity extends Activity {
    private static final String TAG = "ReceiverWriteActivity";


    private TextView tv_receiverEpc;
    private TextView tv_receiverCarNum;
    private TextView tv_shipperMao;
    private TextView tv_shipperPi;
    private TextView tv_shipperJing;

    private EditText et_maoZhong;
    private EditText et_piZhong;
    private TextView tv_jingZhong;
    private TextView tv_resultView;

    private Button btn_receiverReadEpc;
    private Button btn_receiverReading;
    private Button btn_receiverWritting;

    private Handler mHandler = new ReceiverWriteActivity.MainHandler();
    private String m_strresult = "";
    private String spStr[] = null;
    private static String myResult;
    private StringBuilder cardNumData;
    private String maoWeight, piWeight, jingWeight;

    private List<Incidental> incidentals;

    private ImageView imageView;

    private List<CheckBox> CheckBoxs;
    private List<EditText> EditTexts;
    private List<Map<Integer, Boolean>> isTrues = new ArrayList<>();

    private LinearLayout linearLayout;

    private Handler handlerView = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 10000) {
                CheckBoxs = new ArrayList<>();
                EditTexts = new ArrayList<>();

                for (int i = 0; i < incidentals.size(); i++) {
                    setView(i, incidentals.get(i));
                }
            }
        }
    };
    private Bitmap imageBitmap;

    private void setView(int position, Incidental incidental) {
        int i = incidental.getStatus();
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(10, 10, 10, 10);

        LinearLayout la = new LinearLayout(ReceiverWriteActivity.this);
        la.setLayoutParams(llp);
        la.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        vlp.weight = 1;

        CheckBox checkBox = new CheckBox(ReceiverWriteActivity.this);
        checkBox.setLayoutParams(vlp);
        checkBox.setText(incidental.getIncidental());
        CheckBoxs.add(checkBox);

        EditText textView = new EditText(ReceiverWriteActivity.this);
        textView.setLayoutParams(vlp);
        la.addView(checkBox);
        la.addView(textView);
        textView.setText("" + incidental.getMoney());
        linearLayout.addView(la);
        EditTexts.add(textView);

        if (i == 1) {
            textView.setEnabled(false);
        }
    }

    /*
     *设置EPC参数
     * */
    private byte btMemBank;
    private int nadd;
    private int ndatalen;
    private String mimaStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_receiver_write);
//        初始化界面
        initView();
//        注册 hander
        reader.reg_handler(mHandler);
//        事件监听
        onClick();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            Bitmap imageBitmap = (Bitmap) bundle.get("data");
            assert imageBitmap != null;
            imageView.setImageBitmap(imageBitmap);
        }
    }

    private void initView() {

        tv_receiverEpc = (TextView) findViewById(R.id.tv_receiver_epc);
        tv_receiverCarNum = (TextView) findViewById(R.id.tv_receiver_carnum);
        tv_shipperMao = (TextView) findViewById(R.id.tv_shipper_famao);
        tv_shipperPi = (TextView) findViewById(R.id.tv_shipper_fapi);
        tv_shipperJing = (TextView) findViewById(R.id.tv_shipper_fajing);

        et_maoZhong = (EditText) findViewById(R.id.et_maozhong);
        et_piZhong = (EditText) findViewById(R.id.et_pizhong);
        tv_jingZhong = (TextView) findViewById(R.id.tv_jingzhong);
        tv_resultView = (TextView) findViewById(R.id.tv_resultView);

        imageView = (ImageView) findViewById(R.id.receiver_image);

        btn_receiverReadEpc = (Button) findViewById(R.id.btn_receiver_readepc);
        btn_receiverReading = (Button) findViewById(R.id.btn_receiver_reading);
        btn_receiverWritting = (Button) findViewById(R.id.btn_receiver_writting);

        linearLayout = (LinearLayout) findViewById(R.id.reveiver_linear);

        getIncidental();

    }

    private void getIncidental() {
        String url = "http://39.108.0.144/YJYNLogisticsSystem/appIncidental?action=getIncidental";
        Log.v(TAG, "上传地址为：" + url);
        StringRequest getInidental = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (TextUtils.isEmpty(s)) {
                    ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "服务器数据异常");
                    return;
                }
                String name = null;
                try {
                    JSONObject o = new JSONObject(s);
                    incidentals = new ArrayList<>();
                    JSONArray ja = new JSONArray(o.getString("incidental"));
                    int j = ja.length();
                    for (int i = 0; i < j; i++) {
                        JSONObject jo = new JSONObject(String.valueOf(ja.get(i)));
                        Incidental inci = new Incidental();
                        inci.setIncidental(jo.getString("name"));
                        inci.setMoney(jo.getInt("money"));
                        inci.setStatus(jo.getInt("status"));
                        incidentals.add(inci);
                    }
                    handlerView.sendEmptyMessage(10000);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "网络异常，请稍后再试");
            }
        });
        getInidental.setTag(this);
        App.getRequestQueue().add(getInidental);
    }

    private void onClick() {
        btn_receiverReadEpc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readEpc();
            }
        });
        btn_receiverReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readTag();
            }
        });
        btn_receiverWritting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadMeasData();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 1);
                }
            }
        });
    }

    private void contrastReceiver() {
        if (spStr == null) {
            return;
        }
        String url = "http://39.108.0.144/YJYNLogisticsSystem/appPublishInformation?action=getRealordReceiver&";
        StringBuilder stringBuilder = new StringBuilder(url);
        stringBuilder.append("&CARNUM=").append(spStr[1]);
        Log.v(TAG, "上传地址为：" + stringBuilder.toString());
        StringRequest getContactRequest = new StringRequest(stringBuilder.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (TextUtils.isEmpty(s)) {
                    ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "服务器数据异常");
                    return;
                }
                try {
                    JSONObject o = new JSONObject(s);
                    if (o.getString("status").equals("0")) {
                        String localname = Sp.getStrings(ReceiverWriteActivity.this, "name");
                        String intentname = o.getString("name");
                        Log.i(TAG, "onResponse: " + localname + "--" + intentname);
                        if (!localname.equals(intentname)) {
                            ToastUtil.getLongToastByString(ReceiverWriteActivity.this, "所在收货商和订单收货商不符！");
                            finish();
                        } else {
                            ToastUtil.getLongToastByString(ReceiverWriteActivity.this, "通过！");
                        }
                    } else if (o.getString("status").equals("1")) {
                        ToastUtil.getLongToastByString(ReceiverWriteActivity.this, "请先确认是否接取货物！");
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "网络异常，请稍后再试");
            }
        });
        getContactRequest.setTag(this);
        App.getRequestQueue().add(getContactRequest);
    }

    /**
     * 读取EPC
     */
    private void readEpc() {
        reader.InventoryLables();
    }

    /**
     * 读取tag
     */
    private void readTag() {
        if ("".equals(reader.m_strPCEPC)) {
            Toast.makeText(ReceiverWriteActivity.this, "Please select the EPC tags",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //操作区域 EPC(代码为1)
        btMemBank = (byte) 1;
        //起始地址 2
        nadd = 2;
        //读取长度
        ndatalen = 20;
        //密码
        mimaStr = "00000000";

        if (mimaStr == null || mimaStr.equals("")) {
            m_strresult += "Please enter your 8 - digit password!!\n";
            tv_resultView.setText(m_strresult);
            return;
        }
        byte[] passw = reader.stringToBytes(mimaStr);
        byte[] epc = reader.stringToBytes(reader.m_strPCEPC);
        if (null != epc) {
            if (btMemBank == 1) {
                reader.ReadLables(passw, epc.length, epc,
                        (byte) btMemBank, nadd, ndatalen);
            } else {
                reader.ReadLables(passw, epc.length, epc,
                        (byte) btMemBank, nadd, ndatalen);
            }
        }
    }

    /*
    * 写tag
    * */
    /*
    * 写tag
    * */
    private void writeTag() {
        if ("".equals(reader.m_strPCEPC)) {
            Toast.makeText(ReceiverWriteActivity.this, "Please select the EPC tags",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //操作区域 EPC(代码为1)
        btMemBank = (byte) 1;
        //起始地址 2
        nadd = 2;
        //读取长度
        ndatalen = 20;
        //密码
        mimaStr = "00000000";

        if (mimaStr == null || mimaStr.equals("")) {
            m_strresult += "Please enter your 8 - digit password!!\n";
            tv_resultView.setText(m_strresult);
            return;
        }
        byte[] passw = reader.stringToBytes(mimaStr);
        byte[] pwrite = new byte[ndatalen * 2];


        StringBuilder data = new StringBuilder();
        data.append(myResult);
        data.append(",");
        String dataE = data.toString();
        Log.v(TAG, "写入的数据=================" + dataE);

        byte[] myByte = reader.stringToBytes(StringUtils.toHexString(dataE));
        System.arraycopy(myByte, 0, pwrite, 0,
                myByte.length > ndatalen * 2 ? ndatalen * 2
                        : myByte.length);
        byte[] epc = reader.stringToBytes(reader.m_strPCEPC);
        {
            reader.Writelables(passw, epc.length, epc, btMemBank,
                    (byte) nadd, (byte) ndatalen * 2, pwrite);
        }
    }

    /*
    * 上传数据
    * */
    private void uploadMeasData() {
        maoWeight = et_maoZhong.getText().toString().trim();
        piWeight = et_piZhong.getText().toString().trim();
        if (TextUtils.isEmpty(piWeight)) {
            ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "皮重不能为空");
            return;
        } else if (TextUtils.isEmpty(maoWeight)) {
            ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "毛重不能为空");
            return;
        } else {
            Double d_rough_weight = Double.parseDouble(maoWeight);
            Double d_tare = Double.parseDouble(piWeight);
            double d_weight_empty = d_rough_weight - d_tare;
            Log.e(TAG, "d_weight_empty: " + d_weight_empty);
            DecimalFormat df = new DecimalFormat("#.00");
            jingWeight = df.format(d_weight_empty);
            Log.e(TAG, "result: " + jingWeight);
            tv_jingZhong.setText(jingWeight);
        }
        if ("".equals(reader.m_strPCEPC)) {
            Toast.makeText(ReceiverWriteActivity.this, "Please select the EPC tags",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int c = CheckBoxs.size();
        final JSONArray ja = new JSONArray();


        for (int i = 0; i < c; i++) {
            String incidental = URLEncoder.encode(incidentals.get(i).getIncidental());
            JSONObject jo = new JSONObject();
            if (CheckBoxs.get(i).isChecked()) {
                try {
                    String money = EditTexts.get(i).getText().toString().trim();
                    jo.put("incidental", incidental);
                    jo.put("money", money);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    jo.put("incidental", incidental);
                    jo.put("money", "0");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            ja.put(jo);
        }

        Log.i(TAG, "uploadMeasData: " + ja.toString());
        String pn = tv_receiverCarNum.getText().toString().trim();
        if (TextUtils.isEmpty(pn)) {
            ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "车牌号不能为空,请先读卡");
            return;
        } else {
            //String sub_result1 = StringUtils.toStringHex(m_strresult).substring(26, 31);
            if (StringUtils.toStringHex(m_strresult).substring(14, 15).equals("0")) {
                ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "没有发货端写入信息");
            } else {
                //上传计量数据
            /*
            * 参数说明
            * 139.224.0.153：IP
            * LYMistSystem：项目名
            * appPublishInformation：类名
            * uploadMeasData：action
            * CARDNUM：卡编号
            * CARNUM：车牌号
            * RECEIVERMAO：收货端毛重
            * RECEIVERPI：收货端皮重
            * RECEIVERJING：收货端净重
            * INCIDENTAL：杂费信息
            * */
                String url = "http://39.108.0.144/YJYNLogisticsSystem/appPublishInformation?action=uploadMeasData&";
                StringBuilder stringBuilder = new StringBuilder(url);
                stringBuilder.append("CARDNUM=").append(spStr[0]);
                stringBuilder.append("&CARNUM=").append(spStr[1]);
//        try {
//          stringBuilder.append("&SHIPPERMAO=").append(spStr[2]);
//          stringBuilder.append("&SHIPPERPI=").append(spStr[3]);
//          stringBuilder.append("&SHIPPERJING=").append(spStr[4]);
//        }catch (ArrayIndexOutOfBoundsException e){
//          e.printStackTrace();
//          ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "没有发货端写入信息");
//        }

                stringBuilder.append("&RECEIVERMAO=").append(maoWeight);
                stringBuilder.append("&RECEIVERPI=").append(piWeight);
                stringBuilder.append("&RECEIVERJING=").append(jingWeight);
                stringBuilder.append("&INCIDENTAL=").append(ja);


//                Log.v(TAG, "上传地址为：" + stringBuilder.toString());
                Log.i(TAG, "uploadMeasData: " + stringBuilder.toString());
                StringRequest getContactRequest = new StringRequest(stringBuilder.toString(), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (TextUtils.isEmpty(s)) {
                            ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "服务器数据异常");
                            return;
                        }
                        try {
                            JSONObject o = new JSONObject(s);
                            if (o.getString("status").equals("0")) {
                                //上传成功后重新写卡
                                writeTag();
                                if (imageBitmap != null) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //上传发货方图片
                                            String uploadShipperurl = "";
                                            uploadReceiverServer(uploadShipperurl, spStr[1], getBitmapPath(), imageBitmap);
                                        }
                                    }).start();
                                }
                            }
                            ToastUtil.getShortToastByString(ReceiverWriteActivity.this, o.getString("msg"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "网络异常，请稍后再试");
                    }
                });
                getContactRequest.setTag(this);
                App.getRequestQueue().add(getContactRequest);
            }
        }
    }

    private String getBitmapPath() {
        return "receiver" + System.currentTimeMillis() + ".jpg";
    }

    private boolean uploadReceiverServer(String targetUrl, String carnum, String fileName, Bitmap bm) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(targetUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
            // 允许输入输出流
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            // 使用POST方法
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"CARNUM\"" + end);
            dos.writeBytes(end);
            dos.writeBytes(carnum + end);

            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"imagePath\"; filename=\"" + fileName + "\"" + end);
            dos.writeBytes(end);

            dos.write(Util.Bitmap2Bytes(bm));
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();
            JSONObject resultJson = new JSONObject(result);
            int i = resultJson.getInt("status");
            if (i == 0) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "上传成功");
                    }
                });
            }
            dos.close();
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
                httpURLConnection = null;
            }
        }
        return true;
    }

    /*
    * 注册 hander
    * */
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == reader.editepcsmsg) {
                tv_resultView.setText((String) msg.obj);
            }
            //读取tag
            if (msg.what == reader.msgreadwrireepc) {
                if (msg.obj != null) {
                    m_strresult = (String) msg.obj;
                    if (!m_strresult.contains("Error")) {
                         /*
                        * StringUtils.toStringHex(m_strresult).split(",")截取字符串重组数组
                        * substring(startst,endstr)截取从startst到endstr之间的字符
                        * */
                        tv_resultView.setText(StringUtils.toStringHex(m_strresult));
                        spStr = StringUtils.toStringHex(m_strresult).split(",");
                        try {
                            tv_shipperMao.setText(spStr[2]);
                            tv_shipperPi.setText(spStr[3]);
                            tv_shipperJing.setText(spStr[4]);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "没有发货端写入信息");
                        }
                        myResult = spStr[0] + "," + spStr[1];
                        String s = spStr[1];
                        String sub_result = s.substring(0, 1);
                        if (StringUtils.isNumeric(sub_result)) {
                            String sub_result1 = s.substring(1, 7);
                            int province = Integer.valueOf(sub_result);
                            String pv = "";
                            switch (province) {
                                case 1:
                                    pv = "晋";
                                    break;
                                case 2:
                                    pv = "陕";
                                    break;
                                case 3:
                                    pv = "豫";
                                    break;
                                case 4:
                                    pv = "冀";
                                    break;
                                case 5:
                                    pv = "鲁";
                                    break;

                                default:
                                    break;
                            }
                            cardNumData = new StringBuilder();
                            cardNumData.append(pv);
                            cardNumData.append(sub_result1);
                            tv_receiverCarNum.setText(cardNumData.toString());
                        } else {
                            ToastUtil.getShortToastByString(ReceiverWriteActivity.this, "请保证首个字符为数字");
                        }
                    }
                } else {
                    ToastUtil.getShortToastByString(ReceiverWriteActivity.this, m_strresult);
                }
            }
            if (msg.what == reader.msgreadwrite) {
                if (msg.obj != null) {
                    m_strresult = (String) msg.obj;
                    tv_resultView.setText(m_strresult);
                }
            }
            //读卡信息
            if (msg.what == reader.msgreadepc) {
                String readerdata = (String) msg.obj;
                tv_receiverEpc.setText(readerdata);
                reader.m_strPCEPC = readerdata;
            }
            contrastReceiver();
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        App.getRequestQueue().cancelAll(this);
    }
}
