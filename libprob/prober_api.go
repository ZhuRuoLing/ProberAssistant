package libprob

import (
	"io"
	"net/http"
	"net/http/cookiejar"
	"net/url"
	"strconv"
	"strings"
	"time"
)

type proberAPIClient struct {
	cl       http.Client
	maiDiffs []int
}

func newProberAPIClient(networkTimeoutMillis int) (*proberAPIClient, error) {
	return &proberAPIClient{
		cl:       http.Client{Timeout: time.Duration(networkTimeoutMillis) * time.Millisecond},
		maiDiffs: []int{0, 1, 2, 3, 4},
	}, nil
}

func (c *proberAPIClient) fetchDataMaimai(req0 *http.Request, cookies []*http.Cookie) (res map[int][]byte) {
	c.cl.Jar, _ = cookiejar.New(nil)
	if len(cookies) != 2 {
		for _, cookie := range req0.Cookies() {
			if cookie.Name == "userId" {
				cookie2 := *cookies[0]
				cookie2.Name = cookie.Name
				cookie2.Value = cookie.Value
				cookies = append(cookies, &cookie2)
			}
		}
	}
	c.cl.Jar.SetCookies(req0.URL, cookies)

	labels := []string{
		"Basic", "Advanced", "Expert", "Master", "Re: MASTER",
	}
	for _, i := range c.maiDiffs {
		logI("正在导入 %s 难度……", labels[i])
		for {
			res = make(map[int][]byte)
			result, err := c.fetchDataMaimaiPerDiff(i)
			if err != nil {
				res[i] = result
			}
		}
	}
	return
}

func (c *proberAPIClient) fetchDataMaimaiPerDiff(diff int) (result []byte, err error) {
	req, err := http.NewRequest(http.MethodGet, "https://maimai.wahlap.com/maimai-mobile/record/musicSort/search/?search=A&sort=1&playCheck=on&diff="+strconv.Itoa(diff), nil)
	if err != nil {
		logW("从 Wahlap 服务器获取数据失败，正在重试……")
		return
	}
	resp, err := c.cl.Do(req)
	if err != nil {
		logW("从 Wahlap 服务器获取数据失败，正在重试……")
		return
	}
	respText, err := io.ReadAll(resp.Body)
	if err != nil {
		logW("从 Wahlap 服务器获取数据超时，正在重试……您也可以使用命令行参数 -timeout 120 来调整超时时间为 120 秒（默认为 30 秒）")
		return
	}
	return respText, nil
}

func (c *proberAPIClient) fetchDataChuni(req0 *http.Request, cookies []*http.Cookie) (res map[int][]byte) {
	c.cl.Jar, _ = cookiejar.New(nil)
	if len(cookies) != 3 {
		for _, cookie := range req0.Cookies() {
			if cookie.Name == "userId" || cookie.Name == "friendCodeList" {
				cookie2 := *cookies[0]
				cookie2.Name = cookie.Name
				cookie2.Value = cookie.Value
				cookies = append(cookies, &cookie2)
			}
		}
	}
	c.cl.Jar.SetCookies(req0.URL, cookies)
	hds := req0.Header.Clone()
	hds.Del("Cookie")
	labels := []string{
		"Basic 难度", "Advanced 难度", "Expert 难度", "Master 难度", "Ultima 难度", "World's End 难度", "Best 10 ",
	}
	res = make(map[int][]byte)
	for i := 0; i < 7; i++ {
		logI("正在导入 %s……", labels[i])
		result, err := c.fetchDataChuniPerDiff(hds, cookies, i)
		if err != nil {
			res[i] = result
		}
	}
	return
}

func (c *proberAPIClient) fetchDataChuniPerDiff(headers http.Header, cookies []*http.Cookie, diff int) ([]byte, error) {
	postUrls := []string{
		"/record/musicGenre/sendBasic",
		"/record/musicGenre/sendAdvanced",
		"/record/musicGenre/sendExpert",
		"/record/musicGenre/sendMaster",
		"/record/musicGenre/sendUltima",
	}
	urls := []string{
		"/record/musicGenre/basic",
		"/record/musicGenre/advanced",
		"/record/musicGenre/expert",
		"/record/musicGenre/master",
		"/record/musicGenre/ultima",
		"/record/worldsEndList/",
		"/home/playerData/ratingDetailRecent/",
	}
	if diff < 5 {
		formData := url.Values{
			"genre": {"99"},
			"token": {cookies[0].Value},
		}
		req, err := http.NewRequest(http.MethodPost, "https://chunithm.wahlap.com/mobile"+postUrls[diff], strings.NewReader(formData.Encode()))
		if err != nil {
			logW("从 Wahlap 服务器获取数据失败，正在重试……")
			return nil, err
		}
		req.Header = headers
		req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
		_, err = c.cl.Do(req)
		if err != nil {
			logW("从 Wahlap 服务器获取数据失败，正在重试……")
			return nil, err
		}
	}
	req, err := http.NewRequest(http.MethodGet, "https://chunithm.wahlap.com/mobile"+urls[diff], nil)
	if err != nil {
		logW("从 Wahlap 服务器获取数据失败，正在重试……")
		return nil, err
	}
	resp, err := c.cl.Do(req)
	if err != nil {
		logW("从 Wahlap 服务器获取数据失败，正在重试……")
		return nil, err
	}
	result, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}
	return result, nil
}
