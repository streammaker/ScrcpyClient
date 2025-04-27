# ScrcpyClient
分支说明：
    main:                      Video数据传输都采用Tcp,项目基本功能已具备
    further-main:              添加CS通信并且采用Udp,Video数据传输采用Tcp,反转CS部分逻辑
    further-main-2.0:          Udp跨网段通信失败，切换为通信方式为Tcp
    further-main-2.1:          Wifi网络环境下视频传输丢包率较高导致tcp传输队头阻塞,服务端效果不佳,切换Video数据传输方式为Udp