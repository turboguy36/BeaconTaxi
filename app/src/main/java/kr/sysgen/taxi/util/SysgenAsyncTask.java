package kr.sysgen.taxi.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.network.ConnectToServer;

/**
 * Created by leehg on 2016-08-09.
 */
public class SysgenAsyncTask extends AsyncTask<String, Void, String> {
    /**
     * Context
     */
    private final Context mContext;

    /**
     * 접속 할 JSP 파일의 이름
     */
    private String jspFile;

    /**
     * URL Connection 작동
     */
    private ConnectToServer conn;

    /**
     * onPostExecute 에서 작동한다.
     */
    private Listener mListener;

    private ProgressDialog progressDialog;
    private boolean showProgress;

    /**
     * Constructor
     * @param c
     */
    public SysgenAsyncTask(Context c) {
        this.mContext = c;
        conn = new ConnectToServer(mContext);
        setProgressDialog();
    }

    public String getJspFile() {
        return jspFile;
    }

    public void setJspFile(String jspFile) {
        this.jspFile = jspFile;
    }

    public interface Listener{
        public void onResult(String result);
    }

    public Listener getListener() {
        return mListener;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void setProgressDialog() {
        this.progressDialog = new ProgressDialog(mContext);
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.progressDialog.setMessage(mContext.getString(R.string.please_wait));
        this.progressDialog.setCancelable(false);
    }

    public boolean isShowProgress() {
        return showProgress;
    }

    public void showProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }

    @Override
    protected String doInBackground(String... params) {
        String param = params[0];
        return conn.getJson(param, getJspFile());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(isShowProgress())this.getProgressDialog().show();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s != null){
            this.mListener.onResult(s);
        }
        if(isShowProgress())this.getProgressDialog().dismiss();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
