function login(context) {
  return (
    <div className={s.root}>
      <div className={s.container}>
        <form method="post">
          <div className={s.formGroup}>

        <form method="post">
           <div className={s.formGroup}>
            <label className={s.label} htmlFor="usernameOrEmail">
              Username or email address:
            </label>
            <input
              className={s.input}
              id="usernameOrEmail"
              type="text"
              name="usernameOrEmail"
              autoFocus
            />
          </div>

        </form>
          </div>

        </form>

      </div>
    </div>
  );
}