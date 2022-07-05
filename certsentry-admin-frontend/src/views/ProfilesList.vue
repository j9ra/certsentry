<template>
<v-sheet elevation="0" color="grey lighten-5" class="mx-auto">
 <v-card class="pa-5">
  <v-data-table
    :headers="headers"
    :items="profiles"
    item-key="name"
    sort-by="name"
    group-by="territory"
    class="elevation-5"
    show-group-by
    :loading="loading"
    loading-text="Loading... Please wait"
  >
    <template v-slot:top>
      <v-toolbar flat color="blue lighten-4">
        <v-toolbar-title>Validation profiles</v-toolbar-title>
      </v-toolbar>
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
      {{ (errors.length > 0) ? 'Failed to load profiles ' + errors[0].message : '' }}

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
    name: 'Profiles',
    data () {
      return {
        headers: [
          { text: 'Name', align: 'start', value: 'name', groupable: false, },
          { text: 'Territory', value: 'territory', align: 'right' },
          { text: 'Provider', value: 'provider', align: 'right' },
          { text: 'Service Info', value: 'serviceInfo', align: 'right', groupable: false },
          { text: 'Actions', value: 'actions', sortable: false, groupable: false },
        ],
        profiles: [],
        loading: true,
        errors: [],
        error: false,
      
      }
    },
    created () {
      this.initialize()
    },

    methods: {
      initialize () {
        api.getSecured(`/profiles`)
         .then(response => {
           this.profiles = response.data;
          
           this.loading = false;
       })
       .catch(e => {
        console.log('/profiles: response failed:'+ e);
         this.errors.push(e)
         this.error = true
       })

      },
      createItem() {
        this.$router.push({path: `/profiles/list/new`})
      },
      editItem (item) {
        this.editedItem = Object.assign({}, item);
        console.log("Edit " + this.editedItem.name);
        this.$router.push({ path: `/profiles/${this.editedItem.name}/edit` })
      },

      deleteItem (item) {
        confirm(`Are you sure you want to delete this profile: ${item.name}?`) && 
        api.deleteSecured(`/profiles/${item.name}`).then(response => {  
          console.log("Delete: " + response)
          this.initialize() }).catch(e => { this.errors.push(e) })
      },


    }
  }
</script>
