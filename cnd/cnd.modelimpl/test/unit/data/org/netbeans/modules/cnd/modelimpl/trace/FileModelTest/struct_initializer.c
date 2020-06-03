
static const struct
{
    int src;
    int p_dst[16];
    int pf_blend;
} p_blend_cfg[] = {
    { .src = 0, .p_dst = { 0, 0, 0 }, .pf_blend = 0 },
    { .src = 0, .p_dst = { 0, 0, 0 }, .pf_blend = 0 },
    { .src = 0, .p_dst = { 0, 0, 0 }, .pf_blend = 0 },
    { .src = 0, .p_dst = { 0, 0, 0 }, .pf_blend = 0 },
    { .src = 0, .p_dst = { 0, 0, 0 }, .pf_blend = 0 },
    { 0, {0,}, 0 }
};

typedef struct {
    int num;
    int den;
} AVRational;

void foo() {
        AVRational s = (AVRational){1, 2};        
}