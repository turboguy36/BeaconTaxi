package kr.sysgen.taxi.activity.Fragment.TabFragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.activity.MapsActivity;
import kr.sysgen.taxi.adapter.DriversAdapter;
import kr.sysgen.taxi.data.TaxiInfo;

/**
 * Created by leehg on 2016-05-19.
 */
public class DriverInfoTabFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener{
//    private final String TAG = DriverInfoTabFragment.class.getSimpleName();
    private final String TAG = DriverInfoTabFragment.class.getSimpleName();
    private String parameter;
    private final static String ARG_PARAM = "ARG_PARAM";
    private DriversAdapter adapter;
    private ProgressDialog progressDialog;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private Handler mHandler;
    private boolean mScanning;
    private static final long SCAN_PERIOD = 3000;

    private Context mContext;

    boolean isParent = false;

    public static DriverInfoTabFragment newInstance(String param){
        DriverInfoTabFragment fragment = new DriverInfoTabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            parameter = getArguments().getString(ARG_PARAM);
        }

        mContext = getContext();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout view = (RelativeLayout)inflater.inflate(R.layout.tab_driver_info, null);
        TextView emptyTextView = (TextView)view.findViewById(R.id.empty_text);

        ListView list = (ListView)view.findViewById(R.id.listview_drivers);
        adapter = new DriversAdapter(getContext(), R.layout.list_item_driver);
        list.setEmptyView(emptyTextView);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        FloatingActionButton floatingActionButton = (FloatingActionButton)view.findViewById(R.id.floating_button);

        try {
            JSONObject json = new JSONObject(parameter);
            isParent = json.getBoolean(getString(R.string.is_parent));
        }catch(JSONException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        if(!isParent) {
            progressDialog.show();
            scanBluetooth();
        }else{
            emptyTextView.setText("귀하의 자녀가 택시에서 내렸거나, 탑승 하지 않았습니다.");
            floatingActionButton.setOnClickListener(this);
        }

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.floating_button:
                getActivity().startActivity(new Intent(getContext(), MapsActivity.class));
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(!isParent) {
            scanLeDevice(false);
        }
        adapter.clear();
    }

    private void scanBluetooth(){
        final BluetoothManager bluetoothManager = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(mContext, R.string.error_bluetooth_not_supported, Toast.LENGTH_LONG).show();
//            finish();
            return;
        }
        bleScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if(bleScanner == null){
//            finish();
            return;
        }
        mHandler = new Handler();
        scanLeDevice(true);
    }
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bleScanner.stopScan(mScanCallback);
                    progressDialog.dismiss();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bleScanner.startScan(mScanCallback);
        } else {
            mScanning = false;
            if(bleScanner != null) {
                bleScanner.stopScan(mScanCallback);
            }
            progressDialog.dismiss();
        }
    }
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

//            for(ScanResult result: results) {
//                processResult(result);
//            }
        }

        @Override
        public void onScanFailed(int errorCode) {
//            Log.i(TAG, "onScanFailed");
            super.onScanFailed(errorCode);
        }
        private synchronized void processResult(final ScanResult result){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Bundle bundle = getActivity().getIntent().getExtras();

                    scanResult(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                }
            });
        }
    };
    private void scanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if(scanRecord.length > 30) {
            String uuid  = getUUID(scanRecord);
            String major = getMajor(scanRecord);
            String minor = getMinor(scanRecord);

            HashMap<Integer, TaxiInfo> taxiData = new Beacons().getList();
            int beaconMinor = Integer.parseInt(minor);
            if(getString(R.string.reco_uuid).equals(uuid)
                    || getString(R.string.reco_name).equalsIgnoreCase(device.getName())) {
                TaxiInfo taxiInfo = taxiData.get(beaconMinor);
                adapter.addData(taxiInfo);
                adapter.notifyDataSetChanged();
            }
        }
    }
    private String getUUID(byte[] scanRecord) {
        String uuid = IntToHex2(scanRecord[9] & 0xff)
                + IntToHex2(scanRecord[10] & 0xff)
                + IntToHex2(scanRecord[11] & 0xff)
                + IntToHex2(scanRecord[12] & 0xff)
                + "-"
                + IntToHex2(scanRecord[13] & 0xff)
                + IntToHex2(scanRecord[14] & 0xff)
                + "-"
                + IntToHex2(scanRecord[15] & 0xff)
                + IntToHex2(scanRecord[16] & 0xff)
                + "-"
                + IntToHex2(scanRecord[17] & 0xff)
                + IntToHex2(scanRecord[18] & 0xff)
                + "-"
                + IntToHex2(scanRecord[19] & 0xff)
                + IntToHex2(scanRecord[20] & 0xff)
                + IntToHex2(scanRecord[21] & 0xff)
                + IntToHex2(scanRecord[22] & 0xff)
                + IntToHex2(scanRecord[23] & 0xff)
                + IntToHex2(scanRecord[24] & 0xff);
        return uuid;
    }

    private String getMajor(byte[] scanRecord) {
        String hexMajor = IntToHex2(scanRecord[25] & 0xff) + IntToHex2(scanRecord[26] & 0xff);
        return String.valueOf(Integer.parseInt(hexMajor, 16));
    }

    private String getMinor(byte[] scanRecord) {
        String hexMinor = IntToHex2(scanRecord[27] & 0xff) + IntToHex2(scanRecord[28] & 0xff);
        return String.valueOf(Integer.parseInt(hexMinor, 16));
    }
    @SuppressLint("DefaultLocale")
    private String IntToHex2(int i) {
        char hex_2[]     = { Character.forDigit((i >> 4) & 0x0f, 16), Character.forDigit(i & 0x0f, 16) };
        String hex_2_str = new String(hex_2);
        return hex_2_str.toUpperCase();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TaxiInfo taxiInfo = adapter.getItem(position);
        String taxiNumber = taxiInfo.getTaxiNumber();

        StringBuffer buffer = new StringBuffer();
        buffer.append("택시 "+taxiNumber + " 차량에 탑승 하셨습니다. 안심 메시지를 전송 할까요?");
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("문자를 전송 하시겠습니까?")
                .setMessage(buffer.toString())
                .setPositiveButton(R.string.text_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "confirm");
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.text_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "cancel");
                                dialog.dismiss();
                            }
                        });
        builder.show();
    }

    private class Beacons{
        HashMap<Integer, TaxiInfo> list;
        public Beacons(){
            list = new HashMap<>();

            int [] beaconMinors = getResources().getIntArray(R.array.beacon_minors);

            for(int i=0; i<beaconMinors.length; i++){
                int minor = beaconMinors[i];
                TaxiInfo taxiInfo = new TaxiInfo(minor);

                list.put(minor, taxiInfo);
            }
        }

        public HashMap<Integer, TaxiInfo> getList() {
            return list;
        }
    }
}
