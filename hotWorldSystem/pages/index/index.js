//index.js
//获取应用实例
// 这是master上的code test
const app = getApp()
let dateBefore = 0
Page({
  data: {
    topStories: [],
    storyList: []
  },
  //事件处理函数
  bindViewTap: function() {
    wx.navigateTo({
      url: '../logs/logs'
    })
  },
  onShareAppMessage: function(res) {
    if (res.from === 'button') {
      // 来自页面内转发按钮
      console.log(res.target)
    }
    return {
      title: '这是我的毕业论文：基于微信平台的热门文章管理系统的设计与实现，快来体验一下吧！',
      path: 'pages/index/index',
      success: function(res) {
        // 转发成功
        console.log('分享成功')
      },
      fail: function(res) {
        // 转发失败
        console.log('分享失败')
      }
    }
  },
  onLoad: function() {
    wx.showLoading({
      title: '文章加载中...',
    })
    this._fetchNews()
  },
  onReachBottom: function() {
    dateBefore++
    this._fetchNews(getDate(dateBefore))
  },
  _fetchNews(date = "latest", success) {
    let url = `https://news-at.zhihu.com/api/4/news/${date === 'latest' ? date : 'before/' + date}`

    success = success || ((res) => {
      wx.hideLoading();
      let data = res.data
      let topStories = this.data.topStories
      let storyList = this.data.storyList
      let stories = this.data.stories
      data["top_stories"] && topStories.push(...data["top_stories"])
      storyList.push({
        title: this._getTitle(date),
        stories: data["stories"]
      })

      this.setData({
        topStories,
        storyList
      })
    })

    wx.request({
      url,
      success
    })
  },
  _getTitle(dateStr) {
    // 根据date得到标题
    if (dateStr === 'latest') {
      return '今日新闻'
    } else {
      // 测试
      let formatDate = dateStr.replace(/(\d{4})(\d{2})(\d{2})/, '$1-$2-$3')
      let weekMap = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      let date = new Date(formatDate)
      let week = weekMap[date.getDay()]

      let title = dateStr.replace(/\d{4}(\d{2})(\d{2})/, `$1月$2日 ${week}`)
      return title
    }
  }
})

// 获取当前日期
function getDate(before = 0) {
  let date = new Date()
  date.setDate(date.getDate() - before)
  let year = date.getFullYear()
  let month = parseInt(date.getMonth()) + 1
  let day = date.getDate()

  month = month.toString().padStart(2, '0')
  day = day.toString().padStart(2, '0')

  return `${year}${month}${day}`
}