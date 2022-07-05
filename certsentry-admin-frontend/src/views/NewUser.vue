<template>
<v-sheet elevation="0" color="grey lighten-5" class="mx-auto">

 <v-card elevation="5" class="pa-5 ma-5">
  <v-card-title>
    <span class="headline" v-if="editmode">Modify User {{this.id}}</span>
    <span class="headline" v-else>Create New User </span>
  </v-card-title>
  <v-form v-model="valid" ref="form">
    <v-container>
      <v-row>
        <v-col cols="4">
          <v-text-field
            v-model="username"
            ref="username"
            :rules="[
              () => !!username || 'Username is required',
              () => !!username && username.length <= 15 || 'Username must be less than 15 characters'
            ]"
            counter="15"
            label="Username"
            outlined dense
            required
          ></v-text-field>
        </v-col>

        <v-col cols="4">
          <v-text-field
            v-model="password"
            ref="password"
            :rules="[
              () => !!password || 'Password is required',
              () => !!password && password.length <= 20 || 'Password must be less than 20 characters'
            ]"
            counter="20"
            type="password"
            label="Password"
            outlined dense
            required
          ></v-text-field>
        </v-col>

        <v-col cols="4">
          <v-text-field
            v-model="password2"
            ref="password2"
            :rules="[
              () => !!password2 || 'Confirm Password is required',
              () => !!password2 && password2.length <= 20 || 'Confirm Password must be less than 20 characters'
            ]"
            :error-messages="passwordMatchError()"
            counter="20"
            type="password"
            label="Confirm password"
            outlined dense
            required
          ></v-text-field>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="5">
          <v-text-field
            v-model="appname"
            ref="appname"
            :rules="[
              () => (appname == null) ? true : ((appname.length >= 1 && appname.length <= 15) || 'Application name must be between 1 and 15 characters')
            ]"
            counter="15"
            label="Application name"
            outlined dense
          ></v-text-field>
        </v-col>

        <v-col cols="6">
          <v-text-field
            v-model="apikey"
            ref="apikey"
            :rules="[
              () => (apikey == null) ? true : ((apikey.length >= 1 && apikey.length <= 20) || 'API Key must be between 1 and 20 characters')
            ]"
            counter="20"
            label="API Key"
            outlined dense
          ></v-text-field>
        </v-col>
      </v-row>
      <v-row>
      <v-col cols="10">
          <v-text-field
            v-model="appdesc"
            ref="appdesc"
            :rules="[
              () => (appdesc == null) ? true : (appdesc.length <= 80 || 'Description must be less than 80 characters')
            ]"
            counter="80"
            label="Description"
            outlined dense
          ></v-text-field>
        </v-col>
      </v-row>
      <v-row>
      <v-col cols="3">
        <v-combobox
          v-model="select"
          :items="roles"
          label="Role"
          multiple
          outlined
          dense
        ></v-combobox>
      </v-col>
      <v-col cols="2">
       <v-switch v-model="enabled" label="Enabled"></v-switch>
      </v-col>
    </v-row>
    </v-container>
  </v-form>
  
  <v-card-actions>
     <v-btn rounded color="primary" dark @click="saveUser">Save</v-btn>
     <v-btn rounded color="primary" dark @click="resetForm">Reset</v-btn>
    
  </v-card-actions>
 </v-card>

    <v-snackbar v-model="error">
      {{ (errors.length > 0) ? 'Failed to save user ' + errors[0].message : '' }}

      <template v-slot:action="{ attrs }">
        <v-btn
          color="red"
          text
          v-bind="attrs"
          @click="error = false"
        >
          Close
        </v-btn>
      </template>
    </v-snackbar>

</v-sheet>
</template>

<script>
import api from '@/components/http-common.js'

export default {
    name: 'NewUser',
    data: () => ({
      editmode: false,
      valid: false,
      id: '',
      username: '',
      password: '',
      password2: '',
      appname: null,
      apikey: null,
      appdesc: null,
      select: ['USER'],
      roles: [
          'ADMIN',
          'USER',
        ],
      enabled: true,
      errors: [],
      error: false,
    }),
    created () {
      this.fetchData()
    },
    watch: {
      // call again the method if the route changes
      '$route': 'fetchData'
    },

    methods: {
      passwordMatchError() { 
        return (this.password === this.password2) ? '' : 'Password and Confirm Password must match'
      },
      resetForm() {
        this.errors = [];
        this.$refs.form.reset();
        this.fetchData()
      },
      maskPassword() {
        return '**********';
      },
      fetchData () {
       
       // console.log("fetchData "+ this.$route.params.id)

        if(this.$route.params.id  == 'list') 
          return;
        else {
          this.editmode = true;
          this.id = this.$route.params.id
        }
     
        api.getSecured(`/users/${this.id}`)
          .then(response => {
            this.username = response.data.username;
            this.appname = response.data.appname;
            this.apikey = response.data.apikey;
            this.appdesc = response.data.description;
            this.select = response.data.roles.split(',');
            this.enabled = response.data.enabled;
            this.password = this.maskPassword();
            this.password2 = this.maskPassword();
            
            //console.log(response);    
        })
        .catch(e => {
          console.log("error getting user list!");
          console.log(e);
          this.errors.push(e);
          this.error=true
        })
      },

      saveUser() {
        if(this.$refs.form.validate() == false)
          return;
        
        if(this.editmode) {
        
          var data = {
            username: this.username,
            enabled: this.enabled,
            roles: this.select.join(','),
            appname: this.appname,
            apikey: this.apikey,
            description: this.appdesc,
          };

          if(this.password != this.maskPassword()) {
            data.password = this.password;
          }

          api.patchSecured(`/users/${this.id}`, data).
            then(response => {
              this.$router.push({name: 'Users'})
              console.log(response);
          })
          .catch(e => {
            console.log("error");
            console.log(e);
            this.errors.push(e);
            this.error = true
          })

        } else {

         api.postSecured('/users', { 
           username: this.username,
           password: this.password,
           enabled: this.enabled,
           roles: this.select.join(','),
           appname: this.appname,
           apikey: this.apikey,
           description: this.appdesc,
         })
         .then(response => {
           // JSON responses are automatically parsed.
            this.$router.push({name: 'Users'})
           
           console.log(response);
           
          })
          .catch(e => {
            console.log("error");
            console.log(e);
            this.errors.push(e);
            this.error = true
          })
        }

      }
    }
 }
</script>