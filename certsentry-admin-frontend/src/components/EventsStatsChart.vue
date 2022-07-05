<template>
  <v-card elevation="5" class="pa-5">
    <v-card-title class="overline">Events by type</v-card-title>
    
    <div>
      <pie-chart v-if="loaded" :chart-data="chartdata" :chart-labels="chartlabels"></pie-chart>
    </div>

    <v-alert dense  type="error" border="right" colored-border elevation="2" v-if="error">
      Failed to load data: <strong>{{errors[0].message}}</strong>
    </v-alert>

  </v-card>
</template>

<script>
  import PieChart from './PieChart.js'
  import api from '@/components/http-common.js'

  export default {
    components: {
      PieChart
    },
    data: () => ({
      loaded: false,
      chartdata: null,
      chartlabels: null,
      error: false,
      errors: [],
    }),

    mounted () {
      this.initialize();
    },

    methods: {
    initialize () {
        api.getSecured(`/system/eventStats`)
         .then(response => {

          this.loaded = false
          this.chartdata = response.data.values;
          this.chartlabels = response.data.labels;
          this.loaded = true
        })
        .catch(e => {
            console.log("/system/eventStats: response error: " + e);
            this.errors.push(e)
            this.error = true;
        })      
      },
  }
      
    
  }
</script>