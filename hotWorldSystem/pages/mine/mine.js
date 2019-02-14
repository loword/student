//index.js
//获取应用实例
const app = getApp()
var Bmob = require('../../dist/Bmob-1.6.7.min.js');
Page({
  data: {
    motto: 'Hello World',
    userInfo: {},
    hasUserInfo: false,
    canIUse: wx.canIUse('button.open-type.getUserInfo')
  },
  onLoad: function() {
    if (app.globalData.userInfo) {
      this.setData({
        userInfo: app.globalData.userInfo,
        hasUserInfo: true
      })
    } else if (this.data.canIUse) {
      // 由于 getUserInfo 是网络请求，可能会在 Page.onLoad 之后才返回
      // 所以此处加入 callback 以防止这种情况
      app.userInfoReadyCallback = res => {
        this.setData({
          userInfo: res.userInfo,
          hasUserInfo: true
        })
      }
    } else {
      // 在没有 open-type=getUserInfo 版本的兼容处理
      wx.getUserInfo({
        success: res => {
          app.globalData.userInfo = res.userInfo
          this.setData({
            userInfo: res.userInfo,
            hasUserInfo: true
          })
        }
      })
    }
  },
  clickClearAllLike: function(e) {
    let that = this;
    wx.showModal({
      title: '温馨提示',
      content: '确认要删除您收藏的文章吗',
      success(res) {
        if (res.confirm) {
          wx.showLoading({
            title: '删除中...',
            duration: 1000
          })
          const query = Bmob.Query('likePage');
          query.find().then(todos => {
            const obj = JSON.stringify(todos);

            var jsonArray = JSON.parse(obj);

            for (var index in jsonArray) {
              console.log(jsonArray[index].title);
              const query = Bmob.Query('likePage');
              query.equalTo("storyId", "==", jsonArray[index].storyId);
              query.find().then(todos => {
                todos.destroyAll().then(res => {
                  console.log(res, 'ok')
                }).catch(err => {
                  console.log(err);
                })
              });
            }
          })
        }
      }
    })
  },

  clickCheckVersion: function(e) {


    wx.showLoading({
      title: '检查中...',
    })

    setTimeout(function() {
      wx.hideLoading()
      wx.showLoading({
        title: '已是最新版',
        icon: 'success',
        duration: 1000
      })
    }, 1000)
  },

  clickAbout: function(e) {
      wx.navigateTo({
        url: '../about/about'
      })
  },
  getUserInfo: function(e) {
    console.log(e)
    app.globalData.userInfo = e.detail.userInfo
    this.setData({
      userInfo: e.detail.userInfo,
      hasUserInfo: true
    })
  }
})