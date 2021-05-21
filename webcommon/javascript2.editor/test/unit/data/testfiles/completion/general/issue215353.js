function f()
{
    formatter.println(this.msg);
}

f.call({msg:"Ahoj"});
