import Vue from 'vue'
import Vuex from 'vuex'
import api from '@/components/http-common.js'

Vue.use(Vuex)

// inspired by https://pusher.com/tutorials/authentication-vue-vuex

export default new Vuex.Store({
  state: {
    loginSuccess: false,
    loginError: false,
    userName: null,
    userPass: null,
  },
  mutations: {
    login_success(state, payload){
      state.loginSuccess = true;
      state.userName = payload.userName;
      state.userPass = payload.userPass;
      console.log("Login successed: " + payload.userName)
    },
    login_error(state, name){
      state.loginError = true;
      state.userName = name;
      console.log("Login error: " + name)
    }
  },
  actions: {

     login({commit}, {user, password}) {

      return new Promise((resolve,reject) => {
        api.get(`/login`,{
          auth: {
              username: user,
              password: password
          }})
            .then(response => {
                console.log("Response: '" + response.data + "' with Statuscode " + response.status);
                if(response.status == 200) {
                    // place the loginSuccess state into our vuex store
                    commit('login_success', {
                      userName: user,
                      userPass: password
                  });
                }
                resolve(response)
            }).catch(error => {
                console.log("Error: " + error);
                // place the loginError state into our vuex store
                commit('login_error', name);
                reject("Bad credentials")
            })

      })
 
  }

  },
  modules: {
  },
  getters: {
    isLoggedIn: state => state.loginSuccess,
    hasLoginErrored: state => state.loginError,
    getUserName: state => state.userName,
    getUserPass: state => state.userPass,
  }
})
