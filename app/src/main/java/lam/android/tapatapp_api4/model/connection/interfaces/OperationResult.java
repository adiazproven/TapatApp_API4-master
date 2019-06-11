package lam.android.tapatapp_api4.model.connection.interfaces;

import lam.android.tapatapp_api4.model.Child;
import lam.android.tapatapp_api4.model.connection.NegativeResult;
import lam.android.tapatapp_api4.model.Tap;

import java.util.List;

public interface OperationResult {

    public void setObject(Object object);
    public Object getObject();
    public void setChildren(List<Child> children);
    public List<Child> getChildren();
    public void setTaps (List<Tap> taps);
    public List<Tap> getTaps();
    public void setMethod(String method);
    public String getMethod();
    public void setOnError(NegativeResult negativeResult);
    public NegativeResult getOnError();
}
