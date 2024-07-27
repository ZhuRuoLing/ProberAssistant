package libprob

type Platform interface {
	LogI(content string)
	LogW(content string)
	LogE(content string)
	GetLocalProxyPort() int
	NotifyStatus(content string)
	ChuniResultCallback(index int, content []byte)
	MaiResultCallback(index int, content []byte)
	GetCertificateContent() []byte
	GetPrivateKeyContent() []byte
}
