# AudioRecordPlayDemo
此项目是一个功能相对完整的录制mp3格式音频和播放的demo
- 对于录制和播放进行了封装可以作为组件使用；
- 录制的时候对声音采集做了实时监听类似于发语音的动画；
- 录制的音频存在了data/data/包名/cachevoice目录下了，没有做删除操作，需要自行处理

**注意**：项目中用到的libmp3lame.so库，使用时注意Mp3Lame.java文件的路径和文件名一定要跟代码中的一致，因为这是编译so库时写好的路径，如果想要更换路径和文件名，可以参考代码[使用lame编译音频mp3转换的so库](https://github.com/zone-yan/mp3lametest)
