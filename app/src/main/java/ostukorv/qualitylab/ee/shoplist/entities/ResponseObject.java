package ostukorv.qualitylab.ee.shoplist.entities;

import java.util.List;

/**
 * Created by Marko on 2.12.2015.
 */
public class ResponseObject {

    public ResponseObject(boolean isError, String errorMessage, List<?> objects) {
        this.isError = isError;
        this.errorMessage = errorMessage;
        this.objects = objects;
    }

    boolean isError;
    String errorMessage;
    List<?> objects;

    public boolean isError() {
        return isError;
    }

    public void setIsError(boolean isError) {
        this.isError = isError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<?> getObjects() {
        return objects;
    }

    public void setObjects(List<?> objects) {
        this.objects = objects;
    }
}
