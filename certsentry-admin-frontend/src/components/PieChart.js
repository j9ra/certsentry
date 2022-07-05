import { Pie } from 'vue-chartjs'
  export default {
    extends: Pie,
    props: {
      chartData: {
        type: Array | Object,
        required: false
      },
      chartLabels: {
        type: Array,
        required: true
      }
    },
    data () {
      return {
        options: {
          legend: {
            display: true
          },
          cutoutPercentage: 55,
          responsive: true,
          maintainAspectRatio: false
        }
      }
    },
    mounted () {
      this.renderChart({
        labels: this.chartLabels,
        datasets: [
          {
            data: this.chartData,
            backgroundColor: [
              'rgba(250, 106, 0, 255)',  
              'rgba(173, 200, 150, 255)',
                'rgba(150, 181, 180, 255)',
                'rgba(255, 216, 109, 255)',
                
            ]
          }
        ]
      }, this.options)
    }
  }