package kr.sysgen.taxi.network;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import kr.sysgen.taxi.R;
import kr.sysgen.taxi.configure.AppConfig;

/**
 * Created by leehg on 2016-05-18.
 */
public class ConnectToServer {
    private final String TAG = ConnectToServer.class.getSimpleName();
    private Context mContext;

    public static final int RESULT_OK = 1;
    public static final int RESULT_ERROR = 2;
    public static final int RESULT_FAIL = 99;

    public ConnectToServer(Context c){
        this.mContext = c;
    }

    public String getJson(String param, String apiFile) {
        final String serverUrl = AppConfig.RemoteUrl;
        final String urlStr = serverUrl + apiFile;

        Log.i(TAG, urlStr + "?input_data=" +param);
        StringBuilder output = new StringBuilder();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            if (conn != null) {
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Writer writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                writer.write("input_data="+param);
                writer.close();

                int resCode = conn.getResponseCode();

                if (resCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
                    String line = null;
                    while(true) {
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        output.append(line + "\n");
                    }

                    reader.close();
                    conn.disconnect();
                }else if(resCode >= HttpURLConnection.HTTP_BAD_REQUEST && resCode < HttpURLConnection.HTTP_INTERNAL_ERROR){
                    // 클라이언트 요청 오류 (아래 설명 첨부)
                    JSONObject resultJson = new JSONObject();
                    resultJson.put(mContext.getString(R.string.result_code), resCode);
                    resultJson.put(mContext.getString(R.string.error_message), R.string.error_message_400);
                    return resultJson.toString();
                }else if(resCode >= HttpURLConnection.HTTP_INTERNAL_ERROR){
                    // 서버 오류 (아래 설명 첨부)
                    JSONObject resultJson = new JSONObject();
                    resultJson.put(mContext.getString(R.string.result_code), resCode);
                    resultJson.put(mContext.getString(R.string.error_message), R.string.error_message_500);
                    return resultJson.toString();
                }
            }else{
                Log.i(TAG, "conn null");
            }
        } catch(SocketTimeoutException e) {

            return getTimeOutResult();
        } catch(Exception ex) {
            Log.e(TAG, "Exception in processing response.", ex);
            ex.printStackTrace();
            return getTimeOutResult();
        } finally {

        }
        return output.toString();
    }
    private String getTimeOutResult(){
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put(mContext.getString(R.string.result_code), -1);
            resultJson.put(mContext.getString(R.string.error_message), mContext.getString(R.string.error_time_out));
        }catch(JSONException je){
            je.printStackTrace();
        }
        return resultJson.toString();
    }
    /************************************************************************************************
     4xx 클래스의 상태 코드는 클라이언트에 오류가 있음을 나타낸다.

     400(잘못된 요청): 서버가 요청의 구문을 인식하지 못했다.
     401(권한 없음): 이 요청은 인증이 필요하다. 서버는 로그인이 필요한 페이지에 대해 이 요청을 제공할 수 있다.
     403(금지됨): 서버가 요청을 거부하고 있다.
     404(찾을 수 없음): 서버가 요청한 페이지를 찾을 수 없다. 예를 들어 서버에 존재하지 않는 페이지에 대한 요청이 있을 경우 서버는 이 코드를 제공한다.
     405(허용되지 않는 방법): 요청에 지정된 방법을 사용할 수 없다.
     406(허용되지 않음): 요청한 페이지가 요청한 콘텐츠 특성으로 응답할 수 없다.
     407(프록시 인증 필요): 이 상태 코드는 401(권한 없음)과 비슷하지만 요청자가 프록시를 사용하여 인증해야 한다. 서버가 이 응답을 표시하면 요청자가 사용할 프록시를 가리키는 것이기도 한다.
     408(요청 시간초과): 서버의 요청 대기가 시간을 초과하였다.
     409(충돌): 서버가 요청을 수행하는 중에 충돌이 발생했다. 서버는 응답할 때 충돌에 대한 정보를 포함해야 한다. 서버는 PUT 요청과 충돌하는 PUT 요청에 대한 응답으로 이 코드를 요청 간 차이점 목록과 함께 표시해야 한다.
     410(사라짐): 서버는 요청한 리소스가 영구적으로 삭제되었을 때 이 응답을 표시한다. 404(찾을 수 없음) 코드와 비슷하며 이전에 있었지만 더 이상 존재하지 않는 리소스에 대해 404 대신 사용하기도 한다. 리소스가 영구적으로 이동된 경우 301을 사용하여 리소스의 새 위치를 지정해야 한다.
     411(길이 필요): 서버는 유효한 콘텐츠 길이 헤더 입력란 없이는 요청을 수락하지 않는다.
     412(사전조건 실패): 서버가 요청자가 요청 시 부과한 사전조건을 만족하지 않는다.
     413(요청 속성이 너무 큼): 요청이 너무 커서 서버가 처리할 수 없다.
     414(요청 URI가 너무 긺): 요청 URI(일반적으로 URL)가 너무 길어 서버가 처리할 수 없다.
     415(지원되지 않는 미디어 유형): 요청이 요청한 페이지에서 지원하지 않는 형식으로 되어 있다.
     416(처리할 수 없는 요청범위): 요청이 페이지에서 처리할 수 없는 범위에 해당되는 경우 서버는 이 상태 코드를 표시한다.
     417(예상 실패): 서버는 Expect 요청 헤더 입력란의 요구사항을 만족할 수 없다.
     418(I'm a teapot, RFC 2324)
     420(Enhance Your Calm, 트위터)
     422(처리할 수 없는 엔티티, WebDAV; RFC 4918)
     423(잠김,WebDAV; RFC 4918)
     424(실패된 의존성, WebDAV; RFC 4918)
     424(메쏘드 실패, WebDAV)
     425(정렬되지 않은 컬렉션, 인터넷 초안)
     426(업그레이드 필요, RFC 2817)
     428(전제조건 필요, RFC 6585)
     429(너무 많은 요청, RFC 6585)
     431(요청 헤더 필드가 너무 큼, RFC 6585)
     444(응답 없음, Nginx)
     449(다시 시도, 마이크로소프트)
     450(윈도 자녀 보호에 의해 차단됨, 마이크로소프트)
     451(법적인 이유로 이용 불가, 인터넷 초안)
     451(리다이렉션, 마이크로소프트)
     494(요청 헤더가 너무 큼, Nginx)
     495(Cert 오류, Nginx)
     496(Cert 없음, Nginx)
     497(HTTP to HTTPS, Nginx)
     499(클라이언트가 요청을 닫음, Nginx)

     5xx (서버 오류)
     서버가 유효한 요청을 명백하게 수행하지 못했음을 나타낸다.[1]

     500(내부 서버 오류): 서버에 오류가 발생하여 요청을 수행할 수 없다.
     501(구현되지 않음): 서버에 요청을 수행할 수 있는 기능이 없다. 예를 들어 서버가 요청 메소드를 인식하지 못할 때 이 코드를 표시한다.
     502(불량 게이트웨이): 서버가 게이트웨이나 프록시 역할을 하고 있거나 또는 업스트림 서버에서 잘못된 응답을 받았다.
     503(서비스를 사용할 수 없음): 서버가 오버로드되었거나 유지관리를 위해 다운되었기 때문에 현재 서버를 사용할 수 없다. 이는 대개 일시적인 상태이다.
     504(게이트웨이 시간초과): 서버가 게이트웨이나 프록시 역할을 하고 있거나 또는 업스트림 서버에서 제때 요청을 받지 못했다.
     505(HTTP 버전이 지원되지 않음): 서버가 요청에 사용된 HTTP 프로토콜 버전을 지원하지 않는다.
     506(Variant Also Negotiates, RFC 2295)
     507(용량 부족, WebDAV; RFC 4918)
     508(루프 감지됨, WebDAV; RFC 5842)
     509(대역폭 제한 초과, Apache bw/limited extension)
     510(확장되지 않음, RFC 2774)
     511(네트워크 인증 필요, RFC 6585)
     598(네트워크 읽기 시간초과 오류, 알 수 없음)
     599(네트워크 연결 시간초과 오류, 알 수 없음)
     ************************************************************************************************/
}
