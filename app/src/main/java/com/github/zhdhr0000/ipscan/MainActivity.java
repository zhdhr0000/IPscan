package com.github.zhdhr0000.ipscan;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @BindView(R.id.sample_text)
    TextView tv;
    StringBuilder stringBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Example of a call to a native method.
//        tv.setText(stringFromJNI());

        //JsoupDemo,HTMLparser demo.
//        jsoupDemo();

        //Get LAN IPs
        getIPs();

    }

    private void getIPs() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (null != wifiInfo && null != wifiInfo.getSSID()) {
            String info = "getSSID()=" + wifiInfo.getSSID() + "\n"
                    + "getBSSID()=" + wifiInfo.getBSSID() + "\n"
                    + "getHiddenSSID()=" + wifiInfo.getHiddenSSID() + "\n"
                    + "getLinkSpeed()=" + wifiInfo.getLinkSpeed() + "\n"
                    + "getMacAddress()=" + wifiInfo.getMacAddress() + "\n"
                    + "getNetworkId()=" + wifiInfo.getNetworkId() + "\n"
                    + "getIpAddress()=" + intToIp(wifiInfo.getIpAddress()) + "\n"
                    + "getRssi()=" + wifiInfo.getRssi() + "\n"
                    + "getSupplicantState()=" + wifiInfo.getSupplicantState() + "\n"
                    + "getDetailedStateOf()=" + wifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
            tv.setText(info);
            stringBuilder.append(info);
        } else {
            tv.setText("没有连接到wifi");
        }
        stringBuilder.insert(0, "\n");
        tv.setText(stringBuilder.toString());
        String ip = intToIp(wifiInfo.getIpAddress());
        String host = ip.substring(0, ip.lastIndexOf("."));
        checkIp(host);
    }

    private void checkIp(String host) {
        ExecutorService threadPool = Executors.newFixedThreadPool(50);
        final ExecutorService singleThread = Executors.newSingleThreadExecutor();
        for (int i = 1; i < 255; i++) {
            final String pingip = host + "." + i;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (InetAddress.getByName(pingip).isReachable(5000)) {
                            singleThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    stringBuilder.insert(0, pingip + " is Reachable\n");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.e("IPscan", pingip + " is Reachable");
                                            tv.setText(stringBuilder.toString());
                                        }
                                    });
                                }
                            });
                        } else {
                            singleThread.execute(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.i("IPscan", pingip + " is Unreachable");
                                        }
                                    });
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            threadPool.execute(runnable);
        }
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
              ((i >> 8) & 0xFF) + "." +
              ((i >> 16) & 0xFF) + "." +
               (i >> 24 & 0xFF);
    }

    private void jsoupDemo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Elements description = null;
                try {
                    description = Jsoup.connect("https://www.jd.com").get()
                            .select("meta")
                            .select("[name=description]");
                    Log.e("jsoup value", description.attr("content"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
