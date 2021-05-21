img;
for (var img in phone.images) {
}
phone.name;
phone.description;
for (var img in phone.images) {
img;
setImage(img);
}
for (var availability in phone.availability) {
availability;
}
phone.battery.type;
phone.battery.talkTime;
phone.battery.standbyTime;
phone.storage.ram;
phone.storage.flash;
phone.connectivity.cell;
phone.connectivity.wifi;
phone.connectivity.bluetooth;
phone.connectivity.infrared ;
phone.connectivity.gps ;
phone.android.os;
phone.android.ui;
for (var dim in phone.sizeAndWeight.dimensions) {
dim;
}
phone.sizeAndWeight.weight;
phone.display.screenSize;
phone.display.screenResolution;
phone.display.touchScreen ;
phone.hardware.cpu;
phone.hardware.usb;
phone.hardware.audioJack;
phone.hardware.fmRadio ;
phone.hardware.accelerometer ;
phone.camera.primary;
phone.camera.features.join(', ');
phone.additionalFeatures;
