<template>
<v-sheet elevation="0" color="grey lighten-5" class="mx-auto">
 <v-card class="pa-5">
  <v-data-table
    :headers="headers"
    :items="users"
    sort-by="username"
    class="elevation-5"
    :loading="loading"
    loading-text="Loading... Please wait"
  >
    <template v-slot:top>
      <v-toolbar flat color="blue lighten-4">
        <v-toolbar-title>Users (System and Applications)</v-toolbar-title>
      </v-toolbar>
    </template>
   
    <template v-slot:item.enabled="{ item }">
        <v-simple-checkbox v-model="item.enabled" disabled></v-simple-checkbox>
    </template>
   
    <template v-slot:item.actions="{ item }">
      <v-icon small class="mr-2" @click="editItem(item)">
        mdi-pencil
      </v-icon>
      <v-icon small @click="deleteItem(item)">
        mdi-delete
      </v-icon>
    </template>
    <template v-slot:no-data>
      <v-btn color="primary" @click="initialize">Reset</v-btn>
    </template>
  </v-data-table>
  
  <div class="mt-5">
    <v-btn rounded color="primary" dark @click="createItem">Create new</v-btn>
  </div>
  
 </v-card>

    <v-snackbar v-model="error">
      {{ (errors.length > 0) ? 'Failed to load users ' + errors[0].message : '' }}

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
    name: 'UsersList',
    data: () => ({
      headers: [
        { text: 'User name',  value: 'username', align: 'start' },
        { text: 'Roles', value: 'roles' },
        { text: 'Enabled', value: 'enabled' },
        { text: 'App name', value: 'appname' },
        { text: 'Description', value: 'description', sortable: false },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      users: [],
      
      editedItem: {
        username: '',
        roles: '',
        enabled: false,
        appname: '',
        desc: '',
      },
      errors: [],
      error: false,
      loading: true,
    }),

    created () {
      this.initialize()
    },

    methods: {
      initialize () {
        api.getSecured(`/users`)
         .then(response => {
           this.users = response.data;
           this.loading = false;
       })
       .catch(e => {
         console.log('/users: response failed: ' + e);
         this.errors.push(e)
         this.error = true
       })

      },
      editItem (item) {
        this.editedItem = Object.assign({}, item);
        console.log("Edit " + this.editedItem.username);
        this.$router.push({ path: `/users/${this.editedItem.username}/edit` })
      },
      deleteItem (item) {
        confirm(`Are you sure you want to delete this user: ${item.username}?`) && 
        api.deleteSecured(`/users/${item.username}`).then(response => {  
          console.log("Delete: " + response)
          this.initialize() }).catch(e => { this.errors.push(e) })
      },
      createItem() {
        this.$router.push({path: `/users/list/new`})
      },
    },
  }

</script>
