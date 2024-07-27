package libprob

import (
	"crypto/tls"
	"fmt"
	"github.com/elazarl/goproxy"
	"net"
	"net/http"
)

var Plt Platform
var Addr = ":8033"

func UpdatePlatform(inst Platform) {
	Plt = inst
}

func logI(format string, args ...interface{}) {
	ct := fmt.Sprintf(format, args...)
	Plt.LogI(ct)
	Plt.NotifyStatus(ct)
}

func logW(format string, args ...interface{}) {
	ct := fmt.Sprintf(format, args...)
	Plt.LogW(ct)
	Plt.NotifyStatus(ct)
}

func logE(format string, args ...interface{}) {
	ct := fmt.Sprintf(format, args...)
	Plt.LogE(ct)
	Plt.NotifyStatus(ct)
}

func commandFatal(err error) {
	logE("错误：", err)
	Plt.NotifyStatus(err.Error())
}

func patchGoproxyCert() {
	crt := Plt.GetCertificateContent()
	pem := Plt.GetPrivateKeyContent()
	goproxy.GoproxyCa, _ = tls.X509KeyPair(crt, pem)
}

func StartProxy() {
	Addr = fmt.Sprintf(":%d", Plt.GetLocalProxyPort())
	apiClient, err := newProberAPIClient(8000)
	if err != nil {
		commandFatal(err)
	}
	proxyCtx := newProxyContext(apiClient, commandFatal, true)
	patchGoproxyCert()
	srv := proxyCtx.makeProxyServer()
	patchGoproxyCert()
	if host, _, err := net.SplitHostPort(Addr); err == nil && host == "" {
		Addr = "localhost" + Addr
	}
	logI("代理已开启到 %s", Addr)
	patchGoproxyCert()
	logE("错误：", http.ListenAndServe(Addr, srv))
}

func StopProxy() {

}
