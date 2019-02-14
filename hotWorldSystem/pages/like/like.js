// pages/like/like.js
var Bmob = require('../../dist/Bmob-1.6.7.min.js');
Page({

  /**
   * 页面的初始数据
   */
  data: {
    storyList: []
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {

  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function() {
    this.getLikeList();
  },

  getLikeList: function() {
    wx.showLoading({
      title: '文章加载中...',
      duration: 1500
    })
    const query = Bmob.Query('likePage');
    query.find().then(res => {
      wx.hideLoading();

      const obj = JSON.stringify(res);
      this.setData({
        storyList: res
      });
      const isHave = obj != "[]";
      console.log("yes " + obj);
      if (isHave) {
        this.setData({
          showLikeView: true
        })
      } else {
        this.setData({
          showLikeView: false
        })
      }
    });
  },
  handlerDelete: function(e) {
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
          query.equalTo("storyId", "==", e.currentTarget.dataset.bean)
          query.find().then(todos => {
            todos.destroyAll().then(res => {
              console.log(res, 'ok')
              wx.showLoading({
                title: '删除成功',
                duration: 1000
              })
              that.getLikeList();
            }).catch(err => {
              console.log(err);
              wx.showLoading({
                title: '删除失败',
                duration: 1000
              })
            })
          });
        }
      }
    })
  },
  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function() {

  }
})