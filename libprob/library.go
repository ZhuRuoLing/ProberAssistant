package libprob

import (
	"context"
	"crypto/tls"
	"errors"
	"fmt"
	"github.com/elazarl/goproxy"
	"net"
	"net/http"
)

var Plt Platform
var Addr = ":8033"
var tid = 0
var server *http.Server

func UpdatePlatform(inst Platform) {
	Plt = inst
}

func logD(format string, args ...interface{}) {
	ct := fmt.Sprintf(format, args...)
	Plt.LogI("[D]" + ct)
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
	if host, _, err := net.SplitHostPort(Addr); err == nil && host == "" {
		Addr = "localhost" + Addr
	}
	logI("代理已开启到 %s", Addr)
	server = &http.Server{Addr: Addr, Handler: srv}
	err = server.ListenAndServe()
	if err != nil && !errors.Is(err, http.ErrServerClosed) {
		logE("代理服务器意外退出：%s", err.Error())
	}

}

func StopProxy() {
	err := server.Shutdown(context.Background())
	if err != nil {
		logW("停止代理服务器错误: %s", err.Error())
	}
}
