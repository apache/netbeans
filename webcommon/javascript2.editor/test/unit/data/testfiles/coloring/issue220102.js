function test(data, status) {
    window.SYNERGY.session = {
        isLoggedIn: true,
        username: data.username,
        role: data.role,
        created: data.created
    }

}