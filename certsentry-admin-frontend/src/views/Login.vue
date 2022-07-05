<template>
     <v-container class="fill-height" fluid>
        <v-row align="center" justify="center">
          <v-col cols="12" sm="8" md="4">

            <v-overlay opacity="0.9" value="1" z-index="10" >

                <v-card class="elevation-12" light min-width="450">
                <v-toolbar color="primary" dark flat>
                    <v-toolbar-title>Login required</v-toolbar-title>
                    <v-spacer></v-spacer>
                </v-toolbar>
                <v-card-text>
                    <v-form @keyup.native.enter="callLogin()">
                    <v-text-field
                        label="Login"
                        name="login"
                        prepend-icon="mdi-account"
                        type="text"
                        v-model="user"
                    ></v-text-field>

                    <v-text-field
                        id="password"
                        label="Password"
                        name="password"
                        prepend-icon="mdi-lock"
                        type="password"
                        v-model="password"
                    ></v-text-field>
                    </v-form>
                </v-card-text>
                <v-card-actions>
                    <v-alert dense outlined type="error" class="ml-10" v-if="error">
                        Login failed - bad login data
                    </v-alert>
                    <v-spacer></v-spacer>
                    <v-btn color="primary" type="submit" @click="callLogin()">Login</v-btn>
                </v-card-actions>
                </v-card>

            </v-overlay>
          </v-col>
        </v-row>
      </v-container>


</template>

<script>


export default {
  name: 'login',
  data () {
    return {
      user: '',
      password: '',
      error: false
    }
  },
  methods: {
    callLogin() {
        this.$store.dispatch('login', { user: this.user, password: this.password})
        .then(() => this.$router.push({ path: "/"}) )
        .catch(error => {
            console.log("Error");
            console.log(error);
          this.error = error;
        })
    }
  }
}
</script>