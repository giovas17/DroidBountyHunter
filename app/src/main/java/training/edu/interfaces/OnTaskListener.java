package training.edu.interfaces;

/**
 * @author Giovani González
 * Created by darkgeat on 8/20/17.
 */

public interface OnTaskListener {
    void OnTaskCompleted(String response);
    void OnTaskError(int errorCode, String message, String error);
}
