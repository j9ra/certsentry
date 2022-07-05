<template>
    <v-card elevation="5" class="pa-2 ">
        <v-card-title class="overline">{{titlename}}</v-card-title>
        <v-data-table
            :headers="headers"
            :items="items"  
            hide-default-footer
            item-key="updateId"
            >
    
            <template v-slot:item.lastUpdate="{ item }">
              <span>{{ item.lastUpdate | formatDate}}</span>
            </template>

            <template v-slot:item.listIssue="{ item }">
              <span>{{ item.listIssue | formatDate}}</span>
            </template>

             <template v-slot:no-data>
                <v-alert dense  type="error" border="right" colored-border elevation="2" class="mt-5" v-if="error">
                  Failed to load data: <strong>{{errors[0].message}}</strong>
                </v-alert>
               <span v-else>no data</span>
             </template>

        </v-data-table>
    </v-card>
</template>

<script>
import api from '@/components/http-common.js'
import moment from 'moment';

export default {
  data () {
      return {
        titlename: 'Trusted list updates',
        enabled: null,
        items: [],
        headers: [
          { text: 'Territory', align: 'start', sortable: false, value: 'territory', },
          { text: 'Last update', value: 'lastUpdate', sortable: false, },
          { text: 'List issue', value: 'listIssue', sortable: false, },
          { text: 'Update status', value: 'updateStatus', sortable: false, },
          { text: 'Error code', value: 'errorCode', sortable: false, },        
        ],
        error: false,
        errors: [],
      }
    },
    created() {
      this.initialize();
    },
    methods: {
      initialize () {
        api.getSecured(`/trustlists/updates`)
         .then(response => {

           this.loaded = false
           this.items = response.data;
           this.loaded = true
          })
          .catch(e => {
            console.log("/trustlists/updates: response error: " + e);
            this.errors.push(e)
            this.error = true;
          })      
      },
  },
  filters: {
      formatDate(value) {
        if (value) {
          return moment(value).format('YYYY-MM-DD HH:mm')
        }
      }
    },

}
</script>