Android BLE API指南：https://developer.android.google.cn/guide/topics/connectivity/bluetooth-le.html 无需翻墙哟

不是自我介绍的自我介绍：
开聊之前，本人郑重说明：我绝对不是三星粉。
IOS支持BLE的时候，android还不支持BLE,刚好当时的公司做了一个BLE项目（灯泡），我于是关注了android上的BLE，奈何彼时官方不支持，只有某些厂商支持，
网上的资料也是少之又少，一个偶然的机会，我得知三星的某些机型（没记错的话，android4.2.2，同时又是Galaxy系列）支持了BLE，于是乎咬牙（当时工资比较低）
买了一台当时的旗舰机-三星S4，算是踏入了android BLE的大门。过了一段时间，android4.3出来了，终于官方支持了BLE，所以我的前3个BLE项目，兼容了android4.3和
三星galaxy且android4.2.2系列；又过了不长不短一段时间，三星Galaxy的android4.2.2，貌似都升级了android4.3/android4.4（这里不得不说，三星的技术团队还是
比较负责的，机型做升级包的比较多），慢慢的我的新项目就只支持android4.3及以上的机型了。

进入正题：
想做android的BLE开发，模拟器不行了，你的测试机至少要是android4.3(加引号，原因如上)，当然你的手机必须要支持蓝牙4.0，这个绝大部分手机都支持蓝牙4.0了

你应当要了解的几个概念：
服务、特征、描述
一个BLE设备可能有多个服务，一个服务下可能有多个特征，一个特征下又有多个描述
这几个对象（面向对象了）都是以UUID的形式展现；在单片机中，有16位UUID，有128位UUID，IOS可以直接通过16位UUID操作，android不行，但它是有一定规律的，如：
fff1 -> 0000fff1-0000-1000-8000-00805f9b34fb
fff2 -> 0000fff2-0000-1000-8000-00805f9b34fb
聪明的你，应该看到规律了，但有时这也是1个坑，你是做APP的，单片机工程师告诉你服务id是fff1，于是你拿来0000fff1-0000-1000-8000-00805f9b34fb，获取，
get特征，空指针了，你挠头了，其实这也许是单片机工程师忘记调整参数了，如果单片机的代码是128的UUID，你肯定就获取不到了；当然，APP也是有办法确认UUID的情况，
在
    @Override
     public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
         LogAndToastUtil.log("回调onServicesDiscovered()->status:%s", status);
         if (status == BluetoothGatt.GATT_SUCCESS) {
             LogAndToastUtil.log("成功发现服务...mac地址:%s", gatt.getDevice().getAddress());


      List<BluetoothGattService> services = gatt.getServices();
         for (BluetoothGattService service : services) {
            LogAndToastUtil.log("service的uuid:%s", service.getUuid());
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic cc : characteristics) {
               LogAndToastUtil.log("特征的uuid:%s", cc.getUuid());
            }
            LogAndToastUtil.log("-------------------------------------");
         }
       }
     }
 在这里你可以把设备上的服务和特征都打印出来（描述及其多，也没有必要打印），你就可以确认UUID是不是你想要的；注意：LogAndToastUtil.log是我封装的一个日志
 打印工具类；如果你对onServicesDiscovered都不了解，那你只能听我慢慢道来了。

 BLE的流程，扫描->连接->发现服务(discoverService)->然后就能工作了，如果你的BLE设备有发送通知的那么，那么，要在服务发现后，再通知使能
 代码里，我写的比较清楚

 BLE作为一套API，其实是比较简单的，代码也不多，但是要把这套API用好，还是有一些坑的，当年我也踩了无数坑；如果你碰到了问题，可以给我留言，我其实有打算做一个针对
 单片机工程师这个群体，有代码基础，但是无java/android基础的专题，我想如果这些单片机工程师能简单的使用这些，对他们DEBUG，应该是有帮助的；只是不知道是否存在这样的需求；
 刚刚使用git/github，所以对这个也不是很熟悉，写的不好，请见谅。
 如果你做BLE，你肯定会踩到坑，告诉我，也许我的经验可以帮助你。

 如果觉得我的代码对你有帮助，欢迎fork/clone，也希望您能Star，您对我的鼓励，将帮助我继续在Android BLE的道路上继续前行；