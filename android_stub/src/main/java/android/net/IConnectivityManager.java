package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

/**
 * Interface that answers queries about, and allows changing, the
 * state of network connectivity.
 */
public interface IConnectivityManager extends IInterface {
    ProxyInfo getGlobalProxy();

    void setGlobalProxy(ProxyInfo p);
    abstract class Stub extends Binder implements IConnectivityManager{
        public static IConnectivityManager asInterface(IBinder obj) {
            throw new RuntimeException("STUB");
        }
    }
}