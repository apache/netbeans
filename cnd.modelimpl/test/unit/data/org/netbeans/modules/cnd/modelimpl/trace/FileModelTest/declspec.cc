#define Q_DECL_IMPORT __declspec(dllimport)
#define Q_GUI_EXPORT Q_DECL_IMPORT

class Q_GUI_EXPORT A {
public:
   friend Q_GUI_EXPORT A* getA();
};

class QWidgetData {
public:
    QWidgetData() {}
};

class QObject {
public:
    QObject() {}
};

class QPaintDevice {
public:
    QPaintDevice() {}
};

#define __declspec(x) __attribute__((x))

class Q_GUI_EXPORT QWidget : public QObject, public QPaintDevice {
public:
    int i;
    friend Q_GUI_EXPORT QWidgetData *qt_qwidget_data(QWidget *widget);
};
