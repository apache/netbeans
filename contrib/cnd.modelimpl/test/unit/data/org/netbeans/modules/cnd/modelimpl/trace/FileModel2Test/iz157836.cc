int main () {
    register int opcode; /* Current opcode */
    int x;
    switch (opcode) {

        case 12:
            goto slow_get;
            if (1) {
                    goto slow_get;
            } else
                slow_get : x = 14; // <- if this statement inside else { ... } => it's fine



            if (1) {
                goto slow_get;
            }
            break;
    }

    int i;
    for(; i < 10; i++)  label: i++; //
    goto label;

    if(i < 10)  label2: i++; //
    goto label2;

    while(i < 10)  label3: i++; //
    goto label3;

    return 0;
}
