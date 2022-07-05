<template>
<v-sheet elevation="0" color="grey lighten-5" class="mx-auto">

 <v-card elevation="5" class="pa-5 ma-5">
  <v-card-title>
    <span class="headline" v-if="editmode">Modify Profile {{this.id}}</span>
    <span class="headline" v-else>Create New Profile</span>
  </v-card-title>
  <v-form v-model="valid" ref="form">
    <v-container>
      <v-row>
        <v-col cols="2">
          <v-combobox
            v-model="select"
            :items="terrs"
            label="Territory"
            outlined
            dense
          ></v-combobox>
        </v-col>

        <v-col cols="9">
          <v-text-field
            v-model="name"
            ref="name"
            :rules="[
              () => !!name || 'Name is required',
              () => !!name && name.length <= 15 || 'Name must be less than 15 characters',
              () => /^[A-Za-z0-9.-]+$/.test(name) || 'Name must be alfanumeric text (with [.-]) ',
            ]"
            counter="15"
            label="Name"
            outlined dense
            required
          ></v-text-field>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="11">
          <v-text-field
            v-model="provider"
            ref="provider"
            :rules="[
              () => (provider == null) ? true : ((provider.length >= 1 && provider.length <= 40) || 'Provider must be between 1 and 40 characters')
            ]"
            counter="40"
            label="Provider"
            outlined dense
          ></v-text-field>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="11">
          <v-text-field
            v-model="serviceinfo"
            ref="serviceinfo"
            :rules="[
              () => (serviceinfo == null) ? true : ((serviceinfo.length >= 1 &&  serviceinfo.length <= 40) || 'SeviceInfo must be between 1 and 80 characters')
            ]"
            counter="40"
            label="SeviceInfo"
            outlined dense
          ></v-text-field>
        </v-col>
      </v-row>
    </v-container>
  </v-form>
  
  <v-card-actions>
     <v-btn rounded color="primary" dark @click="saveProfile">Save</v-btn>
     <v-btn rounded color="primary" dark>Reset</v-btn>
  </v-card-actions>
 </v-card>


 <v-snackbar v-model="error">
      {{ (errors.length > 0) ? 'Failed to save profile ' + errors[0].message : '' }}

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
    name: 'NewProfile',
    data: () => ({
      id: '',
      editmode: false,
      valid: false,
      errors: [],
      error: false,
      name: '',
      provider: null,
      serviceinfo: null,
      select: ['PL'],
      terrs: ['AT','BE','BG','CY','CZ','DE','DK','EE','EL','ES','EU','FI','FR','HR','HU','IE','IS','IT','LI','LT','LU','LV','MT','NL','NO','PL','PT','RO','SE','SI','SK','UK'],
    }),
    created () {
      this.fetchData()
    },
    watch: {
      // call again the method if the route changes
      '$route': 'fetchData'
    },
    methods: {
      fetchData () {
       
      

        if(this.$route.params.id  == 'list') 
          return;
        else {
          this.editmode = true;
          this.id = this.$route.params.id
        }
     
        api.getSecured(`/profiles/${this.id}`)
          .then(response => {
            this.name = response.data.name;
            this.provider = response.data.provider;
            this.serviceinfo = response.data.serviceinfo;
            this.select = response.data.territory.split(",");
            
            //console.log(response);    
        })
        .catch(e => {
          console.log("error getting profile list!");
          console.log(e);
          this.errors.push(e);
          this.error=true;
        })
      },
      saveProfile() {
        if(this.$refs.form.validate() == false)
          return;
        
        if(this.editmode) {
    
          api.patchSecured(`/profiles/${this.id}`, { 
              name: this.name,
              provider: this.provider,
              territory: this.select,
              serviceinfo: this.serviceinfo,
            }).
            then(response => {
              this.$router.push({name: 'Profiles'})
              console.log(response);
          })
          .catch(e => {
            console.log("error");
            console.log(e);
            this.errors.push(e);
            this.error=true;
          })

        } else {

         api.postSecured('/profiles', { 
              name: this.name,
              provider: this.provider,
              territory: this.select,
              serviceinfo: this.serviceinfo 
         })
         .then(response => {
           // JSON responses are automatically parsed.
            this.$router.push({name: 'Profiles'})
           
           console.log(response);
           
          })
          .catch(e => {
            console.log("error");
            console.log(e);
            this.errors.push(e);
          this.error=true;
          })
        }
      },
    }
}
</script>