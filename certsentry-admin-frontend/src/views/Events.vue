<template>
  <v-sheet elevation="0" color="grey lighten-5" class="mx-auto">
    <v-card class="pa-5" elevation="5">  
      <v-data-table
        :headers="headers"
        :items="events"
        :search="search"  
        class="elevation-5"
      >
        <template v-slot:top>
          <v-toolbar flat color="blue lighten-4">
            <v-toolbar-title>Events</v-toolbar-title>
          </v-toolbar>
            <v-row align-content="center">
                <v-col cols="12" >
                  <v-text-field class="ml-5 mr-5"
                          v-model="search"
                          append-icon="mdi-search-web"
                          label="Search"
                          single-line
                          hide-details
                          :loading="loading"
                          loading-text="Loading... Please wait"
                        ></v-text-field>
                </v-col>
            </v-row>
        </template>

         <template v-slot:item.timestamp="{ item }">
           <span>{{ item.timestamp | formatDate}}</span>
         </template>
      
        <template v-slot:item.type="{ item }">
          <v-chip :color="getColor(item.type)" dark small>{{ item.type }}</v-chip>
        </template>

        <template v-slot:item.description="{ item }">
           <span>{{ item.description.split('|').join('\n') }}</span>
         </template>

        <template v-slot:no-data>
          <v-btn color="primary" @click="initialize">Reset</v-btn>
        </template>
      </v-data-table>
    </v-card>

    <v-snackbar v-model="error">
      {{ (errors.length > 0) ? 'Failed to load events ' + errors[0].message : '' }}

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
import moment from 'moment';

// OK
  export default {
    name: 'Events',
    
    data: () => ({
      loading: true,
      headers: [
        { text: 'Timestamp',  value: 'timestamp', align: 'start', filterable: false, width: "210" },
        { text: 'Type', value: 'type', align: 'start', width: "120" },
        { text: 'Source', value: 'source', width: "140" },
        { text: 'Username', value: 'secUser.username', width: "150" },
        { text: 'Description', value: 'description', sortable: false },
      ],
      events: [],
      search: '',
      errors: [],
      error: false,
    }),

    created () {
      this.initialize()
    },

    methods: {
      initialize () {
        api.getSecured(`/events`)
         .then(response => {
           this.events = response.data;
           this.loading = false;
          })
          .catch(e => {
            console.log('/events: response error: ' + e);
            this.errors.push(e)
            this.error = true
          })      
      },
      getColor (type) {
        if (type == 'ERROR') return 'red'
        else if (type == 'WARN') return 'orange'
        else if (type == 'INFO') return 'green'
        else return 'gray'
      },
    },
    filters: {
      formatDate(value) {
        if (value) {
          return moment(value).format('YYYY-MM-DD HH:mm:ss.SSS')
        }
      }
    },
  }

</script>
