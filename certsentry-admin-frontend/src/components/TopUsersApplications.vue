<template>
  <v-card elevation="5" class="pa-2" height="350">
    <v-card-title class="overline">{{ titlename }}</v-card-title>

    <v-divider></v-divider>

    <v-list dense>
      <v-list-item v-for="item in usersapps" :key="item.name">
        <v-list-item-content>{{item.name}}</v-list-item-content>
        <v-list-item-content class="align-end justify-end">{{ item.count }}</v-list-item-content>
      </v-list-item>
    </v-list>

    <v-alert dense  type="error" border="right" colored-border elevation="2" v-if="error">
       Failed to load data: <strong>{{errors[0].message}}</strong>
    </v-alert>

  </v-card>
</template>

<script>
import api from '@/components/http-common.js'

export default {
  data: () => ({
    titlename: "Top Applications",
    usersapps: [],
    error: false,
    errors: [],
  }),

  mounted () {
    this.initialize();
  },

  methods: {
    initialize () {
        api.getSecured(`/system/appsTop`)
         .then(response => {

            this.loaded = false

            var labels = response.data.labels;
            var vals = response.data.values;

            for(var i=0;i<labels.length;i++) {
              this.usersapps.push( { name: labels[i], count: vals[i] });
            }
                    
            this.loaded = true
          })
          .catch(e => {
            console.log('/system/appsTop: response error: ' + e);
            this.errors.push(e)
            this.error = true
          })      
      },
  }

};
</script>